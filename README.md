![alt](https://github.com/liuzhushaonian/Lin15/blob/be5e2a9f0ef55b20d0aa052f6700d34c45e2d9f6/screen_short/Screenshot_Trebuchet_20180624-185210.png)

![alt](https://github.com/liuzhushaonian/Lin15/blob/be5e2a9f0ef55b20d0aa052f6700d34c45e2d9f6/screen_short/Screenshot_Trebuchet_20180624-185230.png)

# lin16

# 更新记录

## 0.1

- 支持P
- 支持圆角
- 支持卷轴
- 

# 原理

- 1.从Android P(9.0)开始，快速设置面板就变得稍微有点简单了，它的背景就是那一整块白色的部分，我称之为背景view，没错，它其实就是一个View，一个纯粹的View，但是它有圆角，有背景颜色，有宽和高。
- 2.针对这样一个View，我无法像lin15那时一样，将其直接拿来使用，因为从根本上来说是很难做到的。所以我干脆直接new了一个View，给这个View设置上一样的宽高，圆角，以及位置都与它一模一样，说白了就是顶替它的存在。
- 3.顶替的时候发现，这个背景view还挺皮的，虽然顶替成功，但是却并未见效，不得已我只能将这背景view设置成了透明色。
- 4.在3之前，也就是给这个背景view设置透明色的之前，我尝试过给这个view设置别的颜色，比如原谅色，但是并没有什么卵用，后来我才意识到，这个view并不是简单的给background设置颜色，而是使用了backgroundTint这个属性，所以才让一切的设置背景色失效。
- 5.解决4的问题很简单，干脆在代码里设置backgroundTint为null，Android会默认null为清除这个，那么在这之后就可以给这个背景view正常上颜色了，也成功添加原谅色。
- 6.很可惜4和5都不是我想要的，因为这背景view在1里说过了，就是个纯粹的view，我需要的是一个viewgroup，所以它无法成为操作的主要对象。
- 7.所以主要操作对象还是QSContainerImpl，但并非是给它设置，而是利用它来添加我自定义的view，将自定义view放入其中，并放在背景view前面，避免被背景view遮挡。但很可惜怕什么来什么，背景view还是挡住了。解决方法很简单，因为这个背景view有个```android:elevation="4dp"``` ，这个属性将它升高了，所以放在前面的自定义view一样被其遮挡。所以给自定义view也一样放这个高度，即可达到显示的效果
- 8.不同于lin15，lin16只有一个卷轴模式，可以说舍弃了很多东西。
- 9.更多的功能暂时没想好，以后再说吧



# 开源协议

[LICENSE](https://github.com/liuzhushaonian/Lin15/blob/master/LICENSE)

本项目源码可以随意使用。