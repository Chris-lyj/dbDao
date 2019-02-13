import com.trustxin.WebApplication;
import com.trustxin.dao.BaseDao;
import com.trustxin.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
@Transactional
public class TestPrimaryDB {

    @Autowired
    BaseDao baseDao;

    @Test
    public void dbTest() throws Exception {
        BaseDao.PageSort pageSort = baseDao.getPageSort(1, 10, "id", "desc");
        baseDao.findBySql("select user.id,user.name,user.account,user.password from user user");
        baseDao.findBySql("select user.id,user.name,user.account,user.password from user user", User.class);
        baseDao.findBySqlPageSort("select user.id,user.name,user.account,user.password from user user", pageSort);
        baseDao.findBySqlPageSort("select user.id,user.name,user.account,user.password from user user", User.class, pageSort);
        baseDao.findBySqlParams("select user.id,user.name,user.account,user.password from user user where user.id = ?", 1L);
        baseDao.findBySqlParams("select user.id,user.name,user.account,user.password from user user where user.id = ?", User.class, 1L);
        baseDao.findBySqlPageSortParams("select user.id,user.name,user.account,user.password from user user where user.id = ?", pageSort, 1L);
        baseDao.findBySqlPageSortParams("select user.id,user.name,user.account,user.password from user user where user.id = ?", User.class, pageSort, 1L);
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1L);
        baseDao.findBySqlParamMap("select user.id,user.name,user.account,user.password from user user where user.id = :id", params);
        baseDao.findBySqlParamMap("select user.id,user.name,user.account,user.password from user user where user.id = :id", User.class, params);
        baseDao.findByParseSqlParamMap("select user.id,user.name,user.account,user.password from user user\n where user.id = :id", User.class, params);
        baseDao.findByParseSqlParamMap("select user.id,user.name,user.account,user.password from user user\n where user.id = :id", User.class, new HashMap<>());
        baseDao.findBySqlPageSortParamMap("select user.id,user.name,user.account,user.password from user user where user.id = :id", pageSort, params);
        baseDao.findBySqlPageSortParamMap("select user.id,user.name,user.account,user.password from user user where user.id = :id", User.class,pageSort, params);
        baseDao.findByParseSqlPageSortParamMap("select user.id,user.name,user.account,user.password from user user\n where user.id = :id", User.class,pageSort, params);
        baseDao.findByParseSqlPageSortParamMap("select user.id,user.name,user.account,user.password from user user\n where user.id = :id", User.class,pageSort, new HashMap<>());
        baseDao.executeSql("update authority au set au.id = 2,au.name = '321321'");
        User user = new User();
        user.setId(2L);
        user.setActive(true);
        user.setPassword("dsadsadsad");
        user.setLoginName("dsadsa");
        user.setName("dsadsadsa");
        user.setAccount(new BigDecimal("111.11"));
        System.out.println(baseDao.insertSqlParser(user));
        baseDao.insert(user);
        user = new User();
        user.setId(2L);
        user.setName("dsa");
        System.out.println(baseDao.updateSqlParser(user));
        baseDao.update(user);
        System.out.println(baseDao.deleteSqlParser(user));
        baseDao.delete(user);
    }
}
