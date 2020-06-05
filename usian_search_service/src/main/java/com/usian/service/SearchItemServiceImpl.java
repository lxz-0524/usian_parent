package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.usian.mapper.SearchItemMapper;
import com.usian.pojo.SearchItem;
import com.usian.utils.JsonUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SearchItemServiceImpl implements SearchItemService {

    @Autowired
    private SearchItemMapper searchItemMapper ;

    @Autowired
    private RestHighLevelClient restHighLevelClient ;

    @Value("${ES_INDEX_NAME}")
    private String ES_INDEX_NAME ;

    @Value("${ES_TYPE_NAME}")
    private String ES_TYPE_NAME ;

    @Override
    public boolean importAll() {
        try {
            if (!isExistsIndex()){
                createIndex();
            }
            int page = 1 ;
            while (true){
                //分页每次导入一千条数据
                PageHelper.startPage(page,1000);
                //查询msq中商品信息
                List<SearchItem> itemList = searchItemMapper.getItemList();
                if (itemList==null||itemList.size()==0){
                    break;
                }
                BulkRequest bulkRequest = new BulkRequest();
                for (int i = 0 ;i<itemList.size() ;i++){
                    SearchItem searchItem = itemList.get(i) ;
                    //把商品信息添加到es请求中
                    bulkRequest.add(new IndexRequest(ES_INDEX_NAME,ES_TYPE_NAME).source(JsonUtils.objectToJson(searchItem),XContentType.JSON));
                }
                //完成向ES中添加商品的请求
                restHighLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT);
                page++ ;
            }
            return true ;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SearchItem> selectByQ(String q, Long page, Integer pageSize) {
      try {
          //1、根据条件对商品名称、类别、卖点、描述里面包含了q的商品
          SearchRequest searchRequest = new SearchRequest(ES_INDEX_NAME);
          searchRequest.types(ES_TYPE_NAME);
          SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
          searchSourceBuilder.query(QueryBuilders.multiMatchQuery(q,
                  new String[]{"item_title","item_category_name","item_desc","item_sell_point"}));
          //long num = (page - 1) * pageSize;
          //2、分页
          /**
           * 1  0  20--->(p-1)*pageSize
           * 2  20 20--->(2-1)*20
           * 3  40 20--->(3-1)*20
           */
          Long  from = (page - 1) * pageSize;
          searchSourceBuilder.from(from.intValue());
          searchSourceBuilder.size(pageSize);
          /*searchSourceBuilder.from(Integer.parseInt(String.valueOf(page))-1);
          searchSourceBuilder.size(pageSize);*/
          //3、高亮查询
          HighlightBuilder highlightBuilder = new HighlightBuilder();
          highlightBuilder.preTags("<font color='red'>");
          highlightBuilder.postTags("</font>");
          highlightBuilder.field("item_title");
          searchSourceBuilder.highlighter(highlightBuilder);
          searchRequest.source(searchSourceBuilder);
          SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
          SearchHits searchHits = searchResponse.getHits();
          SearchHit[] hits = searchHits.getHits();
          List<SearchItem> searchItemList = new ArrayList<SearchItem>();
          for (int i = 0 ;i<hits.length;i++){
              SearchHit hit = hits[i];
              SearchItem searchItem = JsonUtils.jsonToPojo(hit.getSourceAsString(), SearchItem.class);
              Map<String, HighlightField> highlightFields = hit.getHighlightFields();
              if (highlightFields!=null&&highlightFields.size()>0){
                  searchItem.setItem_title(highlightFields.get("item_title").getFragments()[0].toString());
              }
              searchItemList.add(searchItem);
          }
          return searchItemList ;
      }catch (Exception e){
          e.printStackTrace();
      }
        return null;
    }

    @Override
    public int insertTbItem(String itemId) throws IOException {
        //根据商品id查询商品信息
        SearchItem searchItem = searchItemMapper.getItemById(itemId);
        IndexRequest indexRequest = new IndexRequest(ES_INDEX_NAME,ES_TYPE_NAME);
        indexRequest.source(JsonUtils.objectToJson(searchItem),XContentType.JSON);
        IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        return response.getShardInfo().getFailed();
    }

    /**
     * 索引库是否存在
     * @return
     * @throws IOException
     */
    private boolean isExistsIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(ES_INDEX_NAME);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 创建索引库
     * @return
     * @throws IOException
     */
    private boolean createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(ES_INDEX_NAME);
        createIndexRequest.settings(Settings.builder().put("number_of_shards",2).
                                                       put("number_of_replicas",1));
        createIndexRequest.mapping(ES_TYPE_NAME,"{\n" +
                "  \"_source\": {\n" +
                "    \"excludes\": [\n" +
                "      \"item_desc\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"item_title\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_sell_point\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_price\": {\n" +
                "      \"type\": \"float\"\n" +
                "    },\n" +
                "    \"item_image\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"index\": false\n" +
                "    },\n" +
                "    \"item_category_name\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_desc\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    }\n" +
                "  }\n" +
                "}", XContentType.JSON);
        //创建索引操作客户端
        IndicesClient indicesClient = restHighLevelClient.indices();
        //创建响应对象
        CreateIndexResponse response = indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);
        //得到响应结果
        return response.isAcknowledged();
    }
}
