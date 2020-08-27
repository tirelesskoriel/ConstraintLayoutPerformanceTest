# ConstraintLayoutPerformanceTest
一些关于android平台约束布局的性能讨论；Some discussion about constraintlayout performance

ConstraintLayout 的性能是很有问题的，我们可以做一个简单的实验：准备一个稍微复杂一些的 ConstraintLayout，然后计算一下onMeasure 方法的耗时，我做的实验结果如下：

> 25490-25490/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 49

ConstraintLayout 的 onmeasure 方法竟然高达 49ms。这在生产环境明显是无法接受的。那我们换一个简单些的布局呢：

> 25939-25939/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 28

28ms，也够长的。所以简单来说，ConstraintLayout 的性能并不足以支撑线上产品。

那为啥 google 还力推 ConstraintLayout，而且主打 ConstraintLayout 的性能呢？这就是最有趣的地方了，上面的实验中我的做法是冷启动app，然后观察日志。下面我在不杀掉应用的情况下，再次开启包含 ConstraintLayout 的页面，并且重复这个操作，我们再看一下 日志，：

> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 48
> 
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 15
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 14
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 12
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 7
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 9
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 5
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 4
> 26456-26456/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 3

我们发现 onmeasure 方法的耗时虽然有些波动，但是整体趋势竟然是逐步降低的，这就怪了，难道是 ConstraintLayout 缓存了一些计算数据？所以才导致多次开启 ConstraintLayout 会有优化效果？我们再做一个实验：冷启动两次，第一次启动应用后，开启A 界面，然后退出，开启B界面，然后第二次启动应用，这次先开启B 界面，然后再开启 A界面。分别记录两次冷启动后操作的数据。直接看数据：

先A 后B：

> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 52  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 14  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 13  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 9  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 7  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 7  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 6  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 5  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 4  

> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 7  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 3  
> 27734-27734/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 2  

先B 后A

> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 48  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 19  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 8  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 7  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 6  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 5  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: B time: 4  
> 
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 7  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 5  
> 28479-28479/com.examples.constraintlayouttest I/sss_ttt_ddd: A time: 3  

从数据来看我们会发现，即使我们只启动了A 界面，B界面中的 ConstraintLayout 也会得到优化效果，相反也是一样。这就从侧面证明我们关于“ConstraintLayout 缓存了一些计算数据” 的这个想法是错误的。甚至我还仔细查看了源码的细节，也没有发现缓存的痕迹。

说到源码，我在这里也非常粗浅的介绍一下 ConstraintLayout 源码。

ConstraintLayout 的组成可以大致分为三个部分：ConstraintWidgetContainer，ConstraintWidget 和 Cassowary 算法（全称 The Cassowary Linear Arithmetic Constraint Solving Algorithm，有兴趣的同学可以自行搜索）。

ConstraintWidgetContainer 相当于引擎，ConstraintWidget 是所有 ConstraintLayout 所包含控件的抽象（每个child 对应一个 ConstraintWidget），ConstraintWidgetContainer 驱动 所有 ConstraintWidget 近入 Cassowary算法封装盒子，Cassowary 将计算好的 ConstraintWidget 输出交给 ConstraintWidgetContainer，这时，所有child 的布局数据都已经被计算出来，ConstraintLayout只需要把它们放在数据指示的位置即可。

所以我们不难看出，ConstraintLayout 的关键核心就是这个 Cassowary 算法。其实大部分带有图形系统的平台，只要涉及到约束布局，就会用到 Cassowary。最典型的例子就是 ios 的auto layout。不过据说ios 13 已经解决了约束算法的性能问题，android 平台既然也在大力推广 ConstraintLayout，那么它是怎么解决性能问题的呢？

这就回到了我们上面讨论的问题，ConstraintLayout 在多次渲染后，onmeasure 的耗时会逐步降低，而且 ConstraintLayout 并没有为了优化性能而缓存数据。那它到底是怎么做到的？

其实说到这里，熟悉android 或者java 虚拟机的同学可能已经隐约猜到了答案。没错，答案就是 JIT（Just In Time Compiler），不清楚的同学建议先暂停一下，然后去了解一下JIT再回来接着读。

简单来说 JIT 会在程序运行时，优化那些总是被执行的代码，让它们变成机器码，这样就加快了这些代码的运行速度。android 平台上对于 JIT 还有些不太一样。android 从2.2 开始引入JIT，然后从 5.0 废弃D虚拟机，开始启用了ART，ART 虚拟机采用OAT 文件来替代 JIT 的效果，所以5.0 以后是没有 JIT 的。然后由于 ART 的种种弊端，从7.0开始，android平台转向了 解释器，JIT 和OAT 三中混合的模式，这样就平衡了安装速度，安装包大小和运行时性能等等一系列问题。

我们的 ConstraintLayout 就是这些模式的受益者。

为了验证我们的猜测，我们继续做实验：

分别准备 4.1 ，6.0 和8.0 的android虚拟机，然后运行我们上面的 ConstraintLayout 工程，如果我们的结论正确，那么我们可以预测一下结果：

1.在4.1上，ConstraintLayout 在多次渲染后 onmeasure 的耗时会逐步降低  
2.在6.0上，ConstraintLayout 不用多次渲染，onmeasure本来就会比较低  
3.在8.0上会和4.4表现相同甚至更优  

接下来我们直接看数据：

android 4.1

> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 21  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 39  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 33  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 33  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 9  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 6230-6230/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  

android 6.0

> 3347-3347/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 2  
> 3347-3347/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 2  
> 3347-3347/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 2  

android 8.0

> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 47  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 16  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 13  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 10  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 11  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 7  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 5  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 5  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 5  
> 31418-31418/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 4  

果然，符合我们预测的数据。然后我们再做一个实验：在4.4 和 8.0上添加如下代码：
\```html
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.examples.constraintlayouttest">

    <application
        android:vmSafeMode="true"
        ....>
           ....
    </application>

</manifest>

再看运行结果：

android 4.1

> 2956-2956/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 31  
> 2956-2956/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 22  
> 2956-2956/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 21  
> 2956-2956/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 31  
> 2956-2956/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 21  

android 8.0

> 32256-32256/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 72  
> 32256-32256/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 71  
> 32256-32256/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 71  
> 32256-32256/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 72  
> 32256-32256/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 69  
> 32256-32256/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 69  

结果也不出所料，android:vmSafeMode = true 会让虚拟机进入安全模式，这时的 JIT 是不生效的。这在一次证明了我们的猜测是正确的。

那么问题来了，如果我就是想用 ConstraintLayout，能不能有什么办法提升 ConstraintLayout 首次渲染性能呢。

答案是，也不能说没有。

java 后端的同学肯定都熟悉一件事，叫做预热。大概的意思就是我们提前运行起程序，然后虚拟机的各种优化都跑起来，然后再将程序交付用户使用，那么肯定是要比冷启动要性能表现更好的。

那么对于 android 平台的 ConstraintLayout 我们可不可以这么做呢。

我们不妨试试，加入下面这串代码:

    thread {
        repeat(30) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.magic_layout, null, false) as? ConstraintLayout

            val m = ConstraintLayout::class.java.getDeclaredMethod(
                "onMeasure",
                Int::class.java,
                Int::class.java
            )
            m.isAccessible = true

            val m2 = ConstraintLayout::class.java.getDeclaredMethod(
                "onLayout",
                Boolean::class.java,
                Int::class.java,
                Int::class.java,
                Int::class.java,
                Int::class.java
            )
            m2.isAccessible = true
    
            m.invoke(
                view,
                View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST)
            )
            m2.invoke(view, true, 0, 0, 2000, 2000)
        }
    }

重复我们的实验，记得关掉 android:vmSafeMode：

4.1

> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 9  
> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 10  
> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 33  
> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  
> 6457-6457/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 8  

8.0

> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 9  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 4  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 5  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 4  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 5  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 4  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 5  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 3  
> 11688-11688/com.examples.constraintlayouttest I/sss_ttt_ddd: time: 4  

果然，我们 ConstraintLayout 首次渲染的性能是有提升的。那么这么做到底好不好呢。

说实话我也不知道。

JIT 会优化哪些代码，什么时候优化，优化效果持续多久， 对于上层应用来说基本上是不可控制的，也是不可知的，所以预热只能是一种非常模糊的不确定的手段。想要解决 ConstraintLayout 性能问题，最正确的方式肯定是优化它的核心算法。但是在我们没有能力优化它的性能时，这种预热做法可能也不失为一种替代的措施。

关于这个预热我们还可以做的是，彻底分析 ConstraintLayout 源码，挑出真正耗时的方法，然后想办法只在预热时针对性的预热部分真正耗时方法。当然这么做也有弊端，ConstraintLayout 代码还在更新中，1.1.3 和最近发布的2.0 已经有了一些改动（主要是功能的拆分还有一些新特性，但是性能貌似更低了），所以我们挑出越是底层的代码预热，日后要修改的可能性就越大。当然，预热本身可能就是一个不合理的操作。

学艺不精，欢迎轻喷。
