package com.taotao.web.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.taotao.common.service.RedisService;
import com.taotao.manage.pojo.ItemDesc;
import com.taotao.manage.pojo.ItemParamItem;
import com.taotao.web.bean.Item;

@Service
public class ItemService {

	@Autowired
	private ApiService apiService;

	@Value("${TAOTAO_MANAGE_URL}")
	private String TAOTAO_MANAGE_URL;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private RedisService redisService;

	private static final String REDIS_KEY = "TAOTAO_WEB_ITEM_DETAIL";
	private static final Integer REDIS_TIME = 60 * 60 * 24;

	/**
	 * 根据商品id查询商品数据
	 * 
	 * 通过后台系统提供的接口服务进行查询
	 * 
	 * @param itemId
	 * @return
	 */
	public Item queryById(Long itemId) {

		String key = REDIS_KEY + itemId;
		try {
			String cacheData = this.redisService.get(key);
			if (StringUtils.isNotEmpty(cacheData)) {
				return MAPPER.readValue(cacheData, Item.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String url = TAOTAO_MANAGE_URL + "/rest/api/item/" + itemId;
		String jsonData = null;
		try {
			jsonData = this.apiService.doGet(url);
			if (StringUtils.isEmpty(jsonData)) {
				return null;
			}

			try {
				this.redisService.set(key, jsonData, REDIS_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return MAPPER.readValue(jsonData, Item.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查询商品描述数据
	 * 
	 * @param itemId
	 * @return
	 */
	public ItemDesc queryDescByItemId(Long itemId) {
		String url = TAOTAO_MANAGE_URL + "/rest/api/item/desc/" + itemId;
		String jsonData = null;
		try {
			jsonData = this.apiService.doGet(url);
			if (StringUtils.isEmpty(jsonData)) {
				return null;
			}
			return MAPPER.readValue(jsonData, ItemDesc.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String queryItemParamItemByItemId(Long itemId) {
		String url = TAOTAO_MANAGE_URL + "/rest/api/item/param/item/" + itemId;
		String jsonData = null;
		try {
			jsonData = this.apiService.doGet(url);
			if (StringUtils.isEmpty(jsonData)) {
				return null;
			}
			ItemParamItem itemParamItem = MAPPER.readValue(jsonData, ItemParamItem.class);
			String paramData = itemParamItem.getParamData();

			ArrayNode arrayNode = (ArrayNode) MAPPER.readTree(paramData);

			StringBuilder sb = new StringBuilder();
			sb.append(
					"<table cellpadding=\"0\" cellspacing=\"1\" width=\"100%\" border=\"0\" class=\"Ptable\"></table>");

			for (JsonNode param : arrayNode) {
				sb.append("<tr><th class=\"tdTitle\" colspan=\"2\">" + param.get("group").asText() + "</th></tr>");
				ArrayNode params = (ArrayNode) param.get("params");
				for (JsonNode p : params) {
					sb.append("<tr><td class=\"tdTitle\">" + p.get("k").asText() + "</td><td>" + p.get("v").asText()
							+ "</td></tr>");
				}
			}
			sb.append("</tbody></table>");
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
