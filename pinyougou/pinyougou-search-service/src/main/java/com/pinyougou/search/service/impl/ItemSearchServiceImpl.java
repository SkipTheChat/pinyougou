package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate redisTemplate;



//	@Override
//	public Map search(Map searchMap) {
//		Map map=new HashMap();
//
//		Query query=new SimpleQuery("*:*");
//		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
//		query.addCriteria(criteria);
//
//		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//
//		map.put("rows", page.getContent());
//
//		return map;
//	}


	@Override
	public Map search(Map searchMap) {
		Map map=new HashMap();
		//空格处理
		String keywords= (String)searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));//关键字去掉空格

		//1.查询列表
		map.putAll(searchList(searchMap));
		//2.分组查询 商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);

		//3.查询品牌和规格列表
		String category= (String) searchMap.get("category");
		if(!category.equals("")){
			map.putAll(searchBrandAndSpecList(category));
		}else{
			if(categoryList.size()>0){
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}

		return map;
	}


	//查询列表
	private Map searchList(Map searchMap){
		Map map=new HashMap();
		//高亮选项初始化
		HighlightQuery query=new SimpleHighlightQuery();
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//高亮域
		highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项

		//1.1 关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		//1.2 按商品分类过滤
		if(!"".equals(searchMap.get("category"))  )	{//如果用户选择了分类
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}

		//1.3 按品牌过滤
		if(!"".equals(searchMap.get("brand"))  )	{//如果用户选择了品牌
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.4 按规格过滤
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key :specMap.keySet()){

				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);

			}

		}

		//1.5按价格过滤
		if(!"".equals(searchMap.get("price"))){
				String[] price = ((String) searchMap.get("price")).split("-");
				if (!price[0].equals("0")) { //如果最低价格不等于0
					FilterQuery filterQuery = new SimpleFilterQuery();
					Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
					filterQuery.addCriteria(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
				if (!price[1].equals("*")) { //如果最高价格不等于*
					FilterQuery filterQuery = new SimpleFilterQuery();
					Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
					filterQuery.addCriteria(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
		}


		//1.6 分页
		Integer pageNo= (Integer) searchMap.get("pageNo");//获取页码
		if(pageNo==null){
			pageNo=1;
		}
		Integer pageSize= (Integer) searchMap.get("pageSize");//获取页大小
		if(pageSize==null){
			pageSize=20;
		}

		query.setOffset( (pageNo-1)*pageSize  );//起始索引
		query.setRows(pageSize);//每页记录数


		//1.7 排序

		String sortValue= (String)searchMap.get("sort");//升序ASC 降序DESC
		String sortField=  (String)searchMap.get("sortField");//排序字段

		if(sortValue!=null && !sortValue.equals("")){

			if(sortValue.equals("ASC")){
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}



		//***********  获取高亮结果集  ***********
		//高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for(HighlightEntry<TbItem> entry:entryList  ){
			//获取高亮列表(高亮域的个数)
			List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
			/*
			for(Highlight h:highlightList){
				List<String> sns = h.getSnipplets();//每个域有可能存储多值
				System.out.println(sns);
			}*/
			if(highlightList.size()>0 &&  highlightList.get(0).getSnipplets().size()>0 ){
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//总页数
		map.put("total", page.getTotalElements());//总记录数
		return map;

	}


	/**
	 * 查询分类列表。聚合
	 * @param searchMap
	 * @return
	 */
	private  List searchCategoryList(Map searchMap){
		List<String> list=new ArrayList();
		Query query=new SimpleQuery("*:*");
		//按照关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项,类似于聚合
		GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for(GroupEntry<TbItem> entry:content){
			list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
		}
		//得到所有item_category的聚合，即分类列表
		return list;
	}

	/**
	 * 查询品牌和规格列表
	 * @param category 分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map=new HashMap();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
		if(typeId!=null){
			//根据模板ID查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);//返回值添加品牌列表
			//根据模板ID查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}


	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品ID"+goodsIdList);
		Query query=new SimpleQuery();
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
