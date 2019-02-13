package com.trustxin.dao;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.util.JdbcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseDao {

    public JdbcTemplate jdbcTemplate;
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BaseDao(JdbcTemplate jdbcTemplate,NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final Pattern paramPattern = Pattern.compile(":\\w+");

    public class PageSort {
        private int page;
        private int size;
        private String by;
        private String sort;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            if (page < 1) {
                this.page = 0;
            } else {
                this.page = page;
            }
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            if (size < 1) {
                this.size = 0;
            } else if (size > 500) {
                this.size = 500;
            } else {
                this.size = size;
            }
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            if (sort!=null&&sort.toUpperCase().equals("DESC")) {
                this.sort = "DESC";
            } else {
                this.sort = "ASC";
            }
        }
    }

    /**
     * 分页配置，可自动产生分页
     *
     * @param page 页数（1开始）
     * @param size 每页数据行数
     * @return
     */
    public PageSort getPageSort(int page, int size) {
        return getPageSort(page, size, null, null);
    }

    /**
     * 排序配置，可自动排序
     *
     * @param by   排序字段
     * @param sort 排序顺序  asc,desc
     * @return
     */
    public PageSort getPageSort(String by, String sort) {
        return getPageSort(0, 0, by, sort);
    }

    /**
     * 分页排序配置，可自动产生分页和排序
     *
     * @param page 页数（1开始）
     * @param size 每页数据行数
     * @param by   排序字段
     * @param sort 排序顺序  asc,desc
     * @return
     */
    public PageSort getPageSort(int page, int size, String by, String sort) {
        PageSort pageSort = new PageSort();
        pageSort.setPage(page);
        pageSort.setSize(size);
        pageSort.setBy(by);
        pageSort.setSort(sort);
        return pageSort;
    }

    /**
     * 分页排序逻辑
     * @param pageSort
     * @return
     */
    private String pageSortToString(PageSort pageSort) throws Exception{
        if(pageSort.getPage()==0&&pageSort.getSize()==0&&StringUtils.isNotBlank(pageSort.getBy())&&StringUtils.isNotBlank(pageSort.getSort())){
            return "\nORDER BY " + pageSort.getBy() + " " + pageSort.getSort();
        }else if(pageSort.getPage()>0&&pageSort.getSize()>0&&StringUtils.isBlank(pageSort.getBy())){
            return "\nLIMIT " + pageSort.getPage() + "," + pageSort.getSize();
        }else if(pageSort.getPage()>0&&pageSort.getSize()>0&&StringUtils.isNotBlank(pageSort.getBy())&&StringUtils.isNotBlank(pageSort.getSort())){
            return "\nORDER BY " + pageSort.getBy() + " " + pageSort.getSort()+"\nLIMIT " + pageSort.getPage() + "," + pageSort.getSize();
        }else{
            throw new Exception("must have one of page and sort");
        }
    };


    /**
     * sql查询
     *
     * @param sql
     * @return
     */
    public List<Map<String, Object>> findBySql(String sql) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * sql查询
     *
     * @param sql
     * @param clazz
     * @return
     */
    public <T> List<T> findBySql(String sql, Class<T> clazz) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 顺序条件(?)查询
     *
     * @param sql
     * @param params
     * @return
     */
    public List<Map<String, Object>> findBySqlParams(String sql, Object... params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return jdbcTemplate.queryForList(sql, params);
    }

    /**
     * 顺序条件(?)查询
     *
     * @param sql
     * @param clazz
     * @param params
     * @return
     */
    public <T> List<T> findBySqlParams(String sql, Class<T> clazz, Object... params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 分页排序查询
     *
     * @param sql
     * @param pageSort
     * @return
     */
    public List<Map<String, Object>> findBySqlPageSort(String sql, PageSort pageSort) throws Exception {
        return findBySqlPageSortParams(sql, pageSort);
    }

    /**
     * 分页排序查询
     *
     * @param sql
     * @param pageSort
     * @return
     */
    public <T> List<T> findBySqlPageSort(String sql, Class<T> clazz, PageSort pageSort) throws Exception {
        return findBySqlPageSortParams(sql, clazz, pageSort);
    }

    /**
     * 顺序条件(?)分页查询
     *
     * @param sql
     * @param pageSort
     * @param params
     * @return
     */
    public List<Map<String, Object>> findBySqlPageSortParams(String sql, PageSort pageSort, Object... params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        if (pageSort == null) {
            throw new Exception("Can not find page");
        }
        StringBuilder stringBuilder = new StringBuilder(sql);
        stringBuilder.append(pageSortToString(pageSort));
        return jdbcTemplate.queryForList(stringBuilder.toString(), params);
    }

    /**
     * 顺序条件(?)分页查询
     *
     * @param sql
     * @param pageSort
     * @param params
     * @return
     */
    public <T> List<T> findBySqlPageSortParams(String sql, Class<T> clazz, PageSort pageSort, Object... params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        if (pageSort == null) {
            throw new Exception("Can not find page");
        }
        StringBuilder stringBuilder = new StringBuilder(sql);
        stringBuilder.append(pageSortToString(pageSort));
        return jdbcTemplate.query(stringBuilder.toString(), params, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 命名条件(:)查询
     *
     * @param sql
     * @param params
     * @return
     */
    public List<Map<String, Object>> findBySqlParamMap(String sql, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return namedParameterJdbcTemplate.queryForList(sql, params);
    }

    /**
     * 命名条件(:)查询
     *
     * @param sql
     * @param params
     * @return
     */
    public <T> List<T> findBySqlParamMap(String sql, Class<T> clazz, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 使用格式化SQL命名条件(:)查询
     *
     * @param sql
     * @param params
     * @return
     */
    public List<Map<String, Object>> findByParseSqlParamMap(String sql, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return namedParameterJdbcTemplate.queryForList(sqlParser(sql,params.keySet()).toString(), params);
    }

    /**
     * 使用格式化SQL命名条件(:)查询
     *
     * @param sql
     * @param params
     * @return
     */
    public <T> List<T> findByParseSqlParamMap(String sql, Class<T> clazz, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return namedParameterJdbcTemplate.query(sqlParser(sql,params.keySet()).toString(), params, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 命名条件(:)分页查询，直接返回Map
     *
     * @param sql
     * @param pageSort
     * @param params
     * @return
     */
    public List<Map<String, Object>> findBySqlPageSortParamMap(String sql, PageSort pageSort, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        if (pageSort == null) {
            throw new Exception("Can not find page");
        }
        StringBuilder stringBuilder = new StringBuilder(sql);
        stringBuilder.append(pageSortToString(pageSort));
        return namedParameterJdbcTemplate.queryForList(stringBuilder.toString(), params);
    }

    /**
     * 命名条件(:)分页查询，直接返回Class
     *
     * @param sql
     * @param pageSort
     * @param params
     * @return
     */
    public <T> List<T> findBySqlPageSortParamMap(String sql, Class<T> clazz, PageSort pageSort, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        if (pageSort == null) {
            throw new Exception("Can not find page");
        }
        StringBuilder stringBuilder = new StringBuilder(sql);
        stringBuilder.append(pageSortToString(pageSort));
        return namedParameterJdbcTemplate.query(stringBuilder.toString(), params, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 使用格式化SQL(:)分页查询，直接返回Map
     *
     * @param sql
     * @param pageSort
     * @param params
     * @return
     */
    public List<Map<String, Object>> findByParseSqlPageSortParamMap(String sql, PageSort pageSort, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        if (pageSort == null) {
            throw new Exception("Can not find page");
        }
        StringBuilder stringBuilder = sqlParser(sql,params.keySet());
        stringBuilder.append(pageSortToString(pageSort));
        return namedParameterJdbcTemplate.queryForList(stringBuilder.toString(), params);
    }

    /**
     * 使用格式化SQL(:)分页查询，直接返回Class
     *
     * @param sql
     * @param pageSort
     * @param params
     * @return
     */
    public <T> List<T> findByParseSqlPageSortParamMap(String sql, Class<T> clazz, PageSort pageSort, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        if (pageSort == null) {
            throw new Exception("Can not find page");
        }
        StringBuilder stringBuilder = sqlParser(sql,params.keySet());
        stringBuilder.append(pageSortToString(pageSort));
        return namedParameterJdbcTemplate.query(stringBuilder.toString(), params, new BeanPropertyRowMapper<>(clazz));
    }

    /**
     * 顺序(?)查询sql行数
     *
     * @param sql
     * @param params
     * @return
     */
    public Integer findCountBySqlParams(String sql, Object... params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        Integer count = jdbcTemplate.queryForObject(toCountSql(sql), params, Integer.class);
        return count;
    }

    /**
     * 命名条件(:)查询sql行数
     *
     * @param sql
     * @param params
     * @return
     */
    public Integer findCountBySqlParamMap(String sql, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        Integer count = namedParameterJdbcTemplate.queryForObject(toCountSql(sql), params, Integer.class);
        return count;
    }

    /**
     * 使用格式化SQL(:)查询sql行数
     *
     * @param sql
     * @param params
     * @return
     */
    public Integer findCountByParseSqlParamMap(String sql, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        Integer count = namedParameterJdbcTemplate.queryForObject(sqlParser(toCountSql(sql),params.keySet()).toString(), params, Integer.class);
        return count;
    }


    /**
     * 提取每行(\n)sql，如未有包含对应行中的占位符，则不加入sql中
     *
     * @param sql
     * @param paramsName
     * @return
     */
    public StringBuilder sqlParser(String sql, Set<String> paramsName) {
        List<String> sqls = Arrays.asList(sql.split("\\n"));
        StringBuilder stringBuilder = new StringBuilder();
        sqls.forEach(item -> {
            stringBuilder.append(checkSqlFromParameter(item, paramsName));
        });
        return stringBuilder;
    }

    /**
     * 检测sql是否包含参数,不包含则不返回
     *
     * @param sql
     * @param paramsName
     * @return
     */
    private String checkSqlFromParameter(String sql, Set<String> paramsName) {
        Matcher matcher = paramPattern.matcher(sql);
        while (matcher.find()) {
            String ms = matcher.group();
            if (!paramsName.contains(ms.substring(1))) {
                return "";
            }
        }
        return sql;
    }

    /**
     * 查询sql转查询数量sql
     *
     * @param sql
     * @return
     */
    private String toCountSql(String sql) {
        //使用mysql解析
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);
        //解析select查询
        SQLSelectStatement sqlStatement = (SQLSelectStatement) stmtList.get(0);
        //获取sql查询块
        SQLSelectQueryBlock sqlSelectQuery = sqlStatement.getSelect().getQueryBlock();
        //创建sql解析的标准化输出
        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor sqlastOutputVisitor = SQLUtils.createFormatOutputVisitor(out, null, JdbcUtils.MYSQL);
        //解析select项
        out.append("SELECT count(*) \n");
        //解析from
        SQLTableSource sqlTableSource = sqlSelectQuery.getFrom();
        if (sqlTableSource != null) {
            out.append("FROM ");
            sqlTableSource.accept(sqlastOutputVisitor);
            out.append("\n");
        }
        //解析where
        SQLExpr sqlExpr = sqlSelectQuery.getWhere();
        if (sqlExpr != null) {
            out.append("WHERE ");
            sqlExpr.accept(sqlastOutputVisitor);
            out.append("\n");
        }
        //解析group
        SQLSelectGroupByClause sqlSelectGroupByClause = sqlSelectQuery.getGroupBy();
        if (sqlSelectGroupByClause != null) {
            sqlSelectGroupByClause.accept(sqlastOutputVisitor);
        }
        return out.toString();
    }

    /**
     * 执行非查询sql
     *
     * @param sql
     * @return 受影响行数
     */
    public int executeSql(String sql) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return jdbcTemplate.update(sql);
    }

    /**
     * 执行非查询sql(?)
     *
     * @param sql
     * @return 受影响行数
     */
    public int executeSqlByParam(String sql, Object... params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return jdbcTemplate.update(sql, params);
    }

    /**
     * 执行非查询sql(:)
     *
     * @param sql
     * @return 受影响行数
     */
    public int executeSqlByParam(String sql, Map<String, Object> params) throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new Exception("Can not find sql");
        }
        return namedParameterJdbcTemplate.update(sql, params);
    }

    /**
     * 批量执行sql
     *
     * @param sqls
     * @throws Exception
     */
    public int[] executeSqlBatch(String... sqls) throws Exception {
        return jdbcTemplate.batchUpdate(sqls);
    }

    /**
     * 属性值转化为sql值
     * @param value
     * @return
     */
    private String value2Sql(Object value) throws Exception{
        if(value==null){
            throw new Exception("the value can not be null");
        }
        if(value.getClass().equals(String.class)){
            return "'"+value+"'";
        }else if(value.getClass().equals(Boolean.class)){
            if(value.equals(true)){
                return "b'1'";
            }else{
                return "b'0'";
            }
        }else{
            return value.toString();
        }
    }

    /**
     * 非空属性格式化为insert-sql,存在注入风险
     * @param entity
     * @throws Exception
     */
    public String insertSqlParser(Object entity) throws Exception {
        Class entityClazz = entity.getClass();
        Field[] fields = entityClazz.getDeclaredFields();
        String tableName = humpToUnderline(toLowCaseFirstOne(entityClazz.getSimpleName()));
        StringBuilder sql = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        int fieldLength = fields.length;
        for (int i=0; i<fieldLength;i++) {   //遍历通过反射获取object的类中的属性名
            Field field = fields[i];
            field.setAccessible(true);    //设置改变属性为可访问
            Object value = field.get(entity);
            if(value!=null){
                sql.append(humpToUnderline(field.getName()));
                values.add(value);
                sql.append(",");
            }
        }
        sql.delete(sql.length()-1,sql.length());
        sql.append(") VALUES(");
        int paramSize = values.size();
        for(int i=0;i<paramSize;i++){
            sql.append(value2Sql(values.get(i)));
            if(i<paramSize-1){
                sql.append(",");
            }
        }
        if(paramSize==0)
            throw new Exception("the attribute is all empty");
        sql.append(")");
        return sql.toString();

    }

    /**
     * 插入对象到数据库
     * @param entity
     * @return
     * @throws Exception
     */
    public int insert(Object entity) throws Exception{
        Class entityClazz = entity.getClass();
        Field[] fields = entityClazz.getDeclaredFields();
        String tableName = humpToUnderline(toLowCaseFirstOne(entityClazz.getSimpleName()));
        StringBuilder sql = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        int fieldLength = fields.length;
        for (int i=0; i<fieldLength;i++) {   //遍历通过反射获取object的类中的属性名
            Field field = fields[i];
            field.setAccessible(true);    //设置改变属性为可访问
            Object value = field.get(entity);
            if(value!=null){
                sql.append(humpToUnderline(field.getName()));
                values.add(value);
                sql.append(",");
            }
        }
        sql.delete(sql.length()-1,sql.length());
        sql.append(") VALUES(");
        int paramSize = values.size();
        for(int i=0;i<paramSize;i++){
            sql.append("?");
            if(i<paramSize-1){
                sql.append(",");
            }
        }
        if(paramSize==0)
            throw new Exception("the attribute is all empty");
        sql.append(")");
        return jdbcTemplate.update(sql.toString(),values.toArray());
    }

    /**
     * 非空属性格式化为update-sql,存在注入风险
     * @param entity
     * @throws Exception
     */
    public String updateSqlParser(Object entity) throws Exception {
        Class entityClazz = entity.getClass();
        Field[] fields = entityClazz.getDeclaredFields();
        String tableName = humpToUnderline(toLowCaseFirstOne(entityClazz.getSimpleName()));
        StringBuilder sql = new StringBuilder();
        Object id = null;
        sql.append("UPDATE ").append(tableName).append(" SET ");
        int fieldLength = fields.length;
        boolean hasValue = false;
        for (int i=0; i<fieldLength;i++) {   //遍历通过反射获取object的类中的属性名
            Field field = fields[i];
            field.setAccessible(true);    //设置改变属性为可访问
            Object value = field.get(entity);
            if(value!=null){
                String name = field.getName();
                if("id".equals(name)){
                    id = value;
                    continue;
                }
                hasValue = true;
                sql.append(humpToUnderline(name)+"="+value2Sql(value));
                sql.append(", ");
            }
        }
        sql.delete(sql.length()-2,sql.length()-1);
        if(!hasValue)
            throw new Exception("the attribute is all empty");
        if(id==null)
            throw new Exception("can not find entity id");
        sql.append(" WHERE id="+value2Sql(id));
        return sql.toString();

    }

    /**
     * 更新对象到数据库
     * @param entity
     * @return
     * @throws Exception
     */
    public int update(Object entity) throws Exception{
        Class entityClazz = entity.getClass();
        Field[] fields = entityClazz.getDeclaredFields();
        String tableName = humpToUnderline(toLowCaseFirstOne(entityClazz.getSimpleName()));
        StringBuilder sql = new StringBuilder();
        List<Object> values = new ArrayList<>();
        Object id = null;
        sql.append("UPDATE ").append(tableName).append(" SET ");
        int fieldLength = fields.length;
        for (int i=0; i<fieldLength;i++) {   //遍历通过反射获取object的类中的属性名
            Field field = fields[i];
            field.setAccessible(true);    //设置改变属性为可访问
            Object value = field.get(entity);
            if(value!=null){
                String name = field.getName();
                if("id".equals(name)){
                    id = value;
                    continue;
                }
                sql.append(humpToUnderline(name)+"=?");
                values.add(value);
                sql.append(", ");
            }
        }
        sql.delete(sql.length()-2,sql.length()-1);
        if(values.size()==0)
            throw new Exception("the attribute is all empty");
        if(id==null)
            throw new Exception("can not find entity id");
        sql.append(" WHERE id=?");
        values.add(id);
        return jdbcTemplate.update(sql.toString(),values.toArray());
    }

    /**
     * 非空属性格式化为delete-sql,存在注入风险
     * @param entity
     * @throws Exception
     */
    public String deleteSqlParser(Object entity) throws Exception {
        Class entityClazz = entity.getClass();
        Field[] fields = entityClazz.getDeclaredFields();
        String tableName = humpToUnderline(toLowCaseFirstOne(entityClazz.getSimpleName()));
        StringBuilder sql = new StringBuilder();
        Object id = null;
        int fieldLength = fields.length;
        for (int i=0; i<fieldLength;i++) {   //遍历通过反射获取object的类中的属性名
            Field field = fields[i];
            field.setAccessible(true);    //设置改变属性为可访问
            Object value = field.get(entity);
            if(value!=null){
                String name = field.getName();
                if("id".equals(name)){
                    id = value;
                    break;
                }
            }
        }
        if(id==null)
            throw new Exception("can not find entity id");
        sql.append("DELETE FROM ").append(tableName).append(" WHERE id="+value2Sql(id));
        return sql.toString();

    }

    /**
     * 删除对象到数据库
     * @param entity
     * @return
     * @throws Exception
     */
    public int delete(Object entity) throws Exception{
        Class entityClazz = entity.getClass();
        Field[] fields = entityClazz.getDeclaredFields();
        String tableName = humpToUnderline(toLowCaseFirstOne(entityClazz.getSimpleName()));
        StringBuilder sql = new StringBuilder();
        Object id = null;
        int fieldLength = fields.length;
        for (int i=0; i<fieldLength;i++) {   //遍历通过反射获取object的类中的属性名
            Field field = fields[i];
            field.setAccessible(true);    //设置改变属性为可访问
            Object value = field.get(entity);
            if(value!=null){
                String name = field.getName();
                if("id".equals(name)){
                    id = value;
                    break;
                }
            }
        }
        if(id==null)
            throw new Exception("can not find entity id");
        sql.append("DELETE FROM ").append(tableName).append(" WHERE id=?");
        return jdbcTemplate.update(sql.toString(),id);
    }

    /**
     * 首字母大写
     * @param s
     * @return
     */
    public String toUpperCaseFirstOne(String s){
        char[] chars = s.toCharArray();
        if(Character.isUpperCase(chars[0])){
            return s;
        }else{
            chars[0] = (char) (chars[0]-32);
            return new String(chars);
        }
    }

    /**
     * 首字母小写
     * @param s
     * @return
     */
    public String toLowCaseFirstOne(String s){
        char[] chars = s.toCharArray();
        if(Character.isLowerCase(chars[0])){
            return s;
        }else{
            chars[0] = (char) (chars[0]+32);
            return new String(chars);
        }
    }

    /**
     * 下划线命名转为驼峰命名
     * @param para 下划线命名的字符串
     */
    public String underlineToHump(String para){
        char[] chars = para.toCharArray();
        StringBuilder result = new StringBuilder();
        boolean toUp = false;
        for(char c: chars){
            if(c=='_'){
                toUp = true;
            }else{
                if(toUp){
                    if(Character.isLowerCase(c)){
                        result.append((char) (c-32));
                    }else{
                        result.append(c);
                    }
                    toUp = false;
                }else{
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    /**
     * 驼峰命名转为下划线命名
     * @param para 驼峰命名的字符串
     */
    public String humpToUnderline(String para){
        char[] chars = para.toCharArray();
        StringBuilder result = new StringBuilder();
        for(char c: chars){
           if(Character.isUpperCase(c)){
               result.append("_").append((char) (c+32));
           }else{
               result.append(c);
           }
        }
        return result.toString();
    }

    /**
     * 结果集属性名转化驼峰式
     * @param results
     * @return
     */
    public List<Map<String, Object>> underlineToHumpResult(List<Map<String, Object>> results){
        List<Map<String, Object>> list = new ArrayList<>();
        for(Map<String,Object> result:results){
            Map<String, Object> map = new HashMap<>(result.size());
            for(Map.Entry<String, Object> entry : result.entrySet()){
                map.put(underlineToHump(entry.getKey()),entry.getValue());
            }
            list.add(map);
        }
        return list;
    }
}
