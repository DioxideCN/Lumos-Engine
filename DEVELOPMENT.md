# 开发指南
开发需要遵守MIT开源协议，如果是发布版本请遵守《阿里巴巴Java开发手册 嵩山版》进行开发。Lumos Engine历时半个月的开发已经形成了非常完备的自动化构建容器和单例容器，拥有非常强大的自动注入和探测能力，得益于这些框架的出现任何一位开发者都能轻松上手Lumos Engine的开发。

## 构建方法
开发者在克隆仓库后需要使用`./gradlew buildSpigot`来进行项目初始化的构建，这个过程会比较漫长（有条件的情况下请配置http proxy），打包使用`./gradlew lumos`进行打包。

## 开发者须知
如你所见到的一样，项目被划分为`Lumos-Spigot`、`Lumos-Common`、`Lumos-Web`他们分别负责Spigot插件启动模块、Common内建核心模块、Web网络接口应用模块。

如果你深入研究过Spring那么对本插件的启动原理也能有一个很好的认知。Spigot插件有自己的生命周期，在挂载`onEnable`方法后，LumosEngine会进行一系列的配置初始化，其大致的步骤如下：

1. 调用Lumos-Common下的`Format.init`方法初始化全局输出配置
2. 调用Lumos-Common下的`Config.init`方法初始化全局配置变量
3. 调用Lumos-Common下的`BeanHolder.init`方法初始化Bean容器
4. 调用Lumos-Common下的`LocalWebEngine.init`方法初始化Web容器
5. 调用Lumos-Spigot下的`CustomRegister.init`方法初始化自定义物品和技能容器

这里我只选择几个重要的初始化方法来详细介绍。

### BeanHolder.init
`BeanHolder.init`首先会加载`Reflect`和`ReflectFactory`将顶层插件类所在包下的类放入缓存容器中进行缓存。在此期间可以通过`@ScanPackage({})`来定义额外的需要扫描的包（这个过程是类似于Spring的包扫描过程的）。

`BeanHolder.init`会自动将属于Spigot的事件、指令、定时任务全部都自动注入到Bukkit中（具体的注入流程感兴趣的朋友可以自行阅读源码，我实习的比较简单因为这也不需要考虑什么循环依赖、三级缓存的问题），标志这些需要被注入的类或方法一般通过`@Event`、`@Executor`、`@LoopThis`来标记，如：

```java
@Event
public class BookDisplayEvent implements Listener {
    // ...
}
```

```java
@Executor(name = "display")
public class DisplayCommand implements TabExecutor {
    // ...
}
```

```java
class Scheduler {
    @LoopThis(period = 50L)
    public static void effectEnhancer() {
        // ...
    }
}
```

`@Event`注解支持从Config中取出Boolean开决定这个事件是否需要被注入，`@Executor`注解需要提供指令名才能被正确注入，`@LoopThis`注解可以自定义任务延迟开始的时间和循环间隔。

### LocalWebEngine.init
`LocalWebEngine.init`方法负责启动Web容器，在有了BeanHolder的支持下LocalWebEngine也具备了接口自动注入的能力。这些接口通常是被`@ServletMapping`注解修饰的`HttpServlet`类，如：

```java
@ServletMapping("/api/player/*")
public class PlayerApiService extends HttpServlet {
    // ...
}
```

LocalWebEngine依赖于插件文件夹下的`application.yml`配置进行Jetty容器的构建。出于数据安全性考虑Web应用只支持MySQL数据库，并通过HikariCP连接池和MyBatis实现数据操作。开发者自定义的Mapper类型应该在`MyBatisConfig`类中手动挂载注入（同时需要配置ORM映射的别名）。

HikariCP和MyBatis都是双锁单例并且具备懒加载机制（你可以类比模组开发中用到的LazyOptional类）开发者定义Mapper的过程中，XML文件必须位于Lumos-Web的`resources/mapper`文件夹下并与`cn.dioxide.web.mapper`包内的Mapper一一映射。

## 构建插件
当你完成了所有的编写工作，请使用Lumos-Spigot的`shadowJar`任务来构建插件，插件会最终生成在`./build/libs/`文件夹中。
