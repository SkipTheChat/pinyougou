# 品优购商城 - 微服务架构  [![author](https://img.shields.io/badge/author-Charlotte-blue.svg?style=flat-square)](#) 

> 高仿项目。非原创。仅供学习。



注意，此项目为未完成状态。若以学习完整项目为目的，请转移到上一项目。若仅想学习dubox如何整合ssm项目，可以一阅。



未完成原因：项目为纯ssm，没有用jpa，springboot，mapper用的是纯xml配置。

但是大量使用了自动生成的mapper.xml文件，以及controller、service类等，很多没有必要的方法和类都被实现了，造成了资源浪费不说，代码可读性也很差。在做到一半的时候还发生了java虚拟机内存溢出的问题。未采用调大内存参数的措施，直接弃了项目。





# 1.项目用到的技术



项目采用前后端分离开发，前端代码已给，在项目的webapp下。

核心的技术栈用的是ssm+dubbox。



### 1.1 前端使用的技术

- Angular

  

### 1.2 后端使用的技术

- SSM
- Dubbox
- Zookeeper
- Solr
- MySQL
- Redis
- FastDFS
- SpringSecurity





# 2.项目模块说明

| 作用                                                   | 模块名                          | 端口 |
| :----------------------------------------------------- | :------------------------------ | :--- |
| 公共模块                                               | pinyougou-common                | 无   |
| 实体类模块                                             | pinyougou-pojo                  | 无   |
| dao实现类（mapper）                                    | pinyougou-dao                   | 无   |
| solr工具类                                             | pinyougou-solr-util             | 无   |
| 接口类                                                 | pinyougou-sellergoods-interface | 无   |
| 接口类                                                 | pinyougou-content-interface     | 无   |
| 接口类                                                 | pinyougou-search-interface      | 无   |
| pinyougou-manager-web&pinyougou-shop-web的后台实现模块 | pinyougou-sellergoods-service   | 无   |
| pinyougou-portal-web的后台实现模块                     | pinyougou-content-service       | 无   |
| pinyougou-search-web的后台实现模块                     | pinyougou-search-service        | 无   |
| 运营商后台（管理商家、广告、商品的审核等）             | pinyougou-manager-web           | 9101 |
| 商家后台（管理商品）                                   | pinyougou-shop-web              | 9102 |
| 网站主页（投放广告功能等）                             | pinyougou-portal-web            | 9103 |
| 用户搜索主页                                           | pinyougou-search-web            | 9104 |



# 3.项目开发进度



### 3.1 运营商后台界面

- 商家审核
- 商家管理
- 品牌管理
- 规格管理
- 模块管理
- 分类管理
- 商品审核
- 广告类型管理
- 广告管理

 





### 3.2 商家后台界面

- 新增商品
- 商品管理





### 3.3 网站主页

- 首页渲染
- 广告的展示





### 3.4 用户搜索主页

- 首页渲染
- 根据关键词搜索
- 根据分类过滤搜索









# 4.项目部署

>部署前请先看注意事项修改相关配置



### 4.1 maven安装部署

1. 对总项目（pinyougou-parent1）进行clean，install
2. 依次tomcat7:run项目，顺序如下：
   - pinyougou-sellergoods-service	
   - pinyougou-content-service
   - pinyougou-search-service
   - pinyougou-manager-web
   - pinyougou-shop-web
   - pinyougou-portal-web
   - pinyougou-search-web



### 4.2 访问网址

>[访问网址：http://localhost:9101](#)

> [访问网址：http://localhost:9102](#)

> [访问网址：http://localhost:9103](#)

> [访问网址：http://localhost:9104](#)





# 5.注意事项

### 5.1 sql文件

> sql文件放在分支sources里面   [资源链接](https://github.com/cristinejssssss/pinyougou/tree/sources)



### 5.2 zookeeper&dubbox&tomcat

开启项目前开启zookeeper和dubbox以及tomcat，并修改相应配置文件地址