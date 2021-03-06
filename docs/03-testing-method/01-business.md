---
order: 1
group:
  title: 单元测试
  order: 3
---

# 业务测试

## 介绍

业务测试也就是对 Service 业务代码进行测试，通常业务代码会有很多的依赖关系。对于业务代码测试最需要注意的[原则](https://github.com/alibaba/p3c/blob/master/p3c-gitbook/%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95.md#L17)有：

- 隔离依赖
- 全自动&非交互式
- 测试粒度足够小
- 遵守 BCDE 原则

## 演示 [Demo](https://github.com/xiyun-international/java-unit-docs/tree/master/source/middle-stage-test-service)

### Service 代码

以用户登录的场景举例：通过手机号查询用户信息后，判断用户是否注册以及登录密码是否正确，并返回信息。

```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * Mockito使用注解注入依赖关系，需提供构造器
     *
     * @param userMapper
     */
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public CallResult login(UserDO userDO) {
        UserDO userResult = userMapper.selectByMobile(userDO.getMobile());
        if (userResult == null) {
            log.info("没有该用户信息，请先注册！");
            return CallResult.fail(CallResult.RETURN_STATUS_UNREGISTERED, "没有该用户信息，请先注册！");
        }
        if (!userDO.getPassword().equals(userResult.getPassword())) {
            return CallResult.fail(CallResult.RETURN_STATUS_PASW_INCORRECT, "您的密码不正确！");
        }
        return CallResult.success(CallResult.RETURN_STATUS_OK, "登录成功！", userResult);
    }
}
```

### 测试代码

- 通过 @BeforeAll 注解在测试方法运行前，准备测试数据。userDO 为用户登录参数，userResult 为模拟的查询结果。
- 通过 when 方法设置当输入参数为 mobile 时，模拟查询过程返回模拟的查询结果。
- 执行业务方法，通过 verify 方法验证模拟的方法是否执行。再通过断言验证返回的业务状态码是否服务我们的预期。

```java
@Slf4j
@SpringBootTest
class MiddleStageTestServiceByAnnotationApplicationTests {

    @Mock
    //通过注解模拟依赖的接口或类
    private UserMapper mockUserMapper;
    @InjectMocks
    //通过注解自动注入依赖关系
    private UserServiceImpl userService;
    //登录参数
    static UserDO userDO;

    //模拟查询结果
    static UserDO userResult;
    static String mobile = "17612345678";
    static String password = "123456";


    @BeforeAll
    static void beforInsertTest() {
        userDO = new UserDO();
        userDO.setMobile(mobile);
        userDO.setPassword(password);

        mockUserResult = new UserDO();
        mockUserResult.setMobile(mobile);
        mockUserResult.setPassword("123456");
    }

    @Test
    @DisplayName("登录测试")
    void login() {
        //验证测试用例是否创建
        Assertions.assertNotNull(userDO, "userDO is null");

        //模拟登录业务中，依赖的dao层查询接口
        UserMapper mockUserMapper = mock(UserMapper.class);
        //将模拟的接口注入
        UserServiceImpl userService = new UserServiceImpl(mockUserMapper);

        //当程序运行时，模拟查询结果，返回我们指定的预期结果
        when(mockUserMapper.selectByMobile(mobile)).thenReturn(mockUserResult);

        CallResult loginCallResult = userService.login(userDO);

        //验证是否执行
        verify(mockUserMapper).selectByMobile(mobile);

        //验证是否与我们预期的状态值相符
        Assertions.assertEquals(CallResult.RETURN_STATUS_OK, loginCallResult.getCode());
        Assertions.assertNotNull(loginCallResult.getContent());
        log.info("[测试通过]");
    }
}
```

### 运行结果

```java
2020-03-15 01:13:22.055  INFO 30348 --- [           main] leStageTestServiceByCodeApplicationTests : [测试通过]
```
