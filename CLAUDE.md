# BlackDex 项目指南

## 项目描述

BlackDex 是一款运行在 Android 手机上的 DEX 脱壳工具，无需 Root、Xposed、Frida 等任何环境依赖，支持 Android 5.0~12。通过构建虚拟运行环境，将目标 APK 安装到虚拟空间中运行，利用 Native 层 Hook 拦截 DEX 文件加载过程并导出（Dump）原始 DEX，实现对一、二、三代壳的脱壳。

- **版本**: 3.2.0 (versionCode 6)
- **包名**: `top.niunaijun.blackdex` (32位: `top.niunaijun.blackdexa32`, 64位: `top.niunaijun.blackdexa64`)
- **作者**: Milk (niunaijun)
- **License**: Apache 2.0

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java (Bcore 核心层), Kotlin (app UI 层), C/C++ (Native 层) |
| 构建 | Gradle 4.2.0, AGP 4.2.0, Kotlin 1.5.0 |
| SDK | compileSdk 30, minSdk 21, targetSdk 30 |
| NDK | 21.4.7075529, CMake |
| 虚拟框架 | VirtualApp (BlackBoxCore) |
| Hook 框架 | Dobby (inline hook), xHook (PLT hook), 自定义 JNI Hook |
| 反射 | FreeReflection (me.weishu:free_reflection:3.0.1) |
| UI | ViewBinding, Material Dialogs, SimpleSearchView, StateView |
| 异步 | Kotlin Coroutines, LiveData, ViewModel |

## 项目结构

```
BlackDex/
├── app/                          # 主应用模块 (Kotlin)
│   └── src/main/java/top/niunaijun/blackdex/
│       ├── app/                  # Application 入口
│       │   ├── App.kt           # Application 类
│       │   ├── AppManager.kt    # 应用管理器
│       │   └── BlackDexLoader.kt # DEX 加载器
│       ├── biz/cache/            # SharedPreferences 缓存
│       ├── data/                 # 数据层
│       │   ├── DexDumpRepository.kt  # 脱壳仓库 (核心业务逻辑)
│       │   └── entity/           # 数据实体 (AppInfo, DumpInfo)
│       ├── util/                 # 工具类 (FileUtil, InjectionUtil)
│       └── view/                 # UI 层
│           ├── base/             # BaseActivity, BaseAdapter, PermissionActivity
│           ├── main/             # MainActivity, MainViewModel, MainAdapter
│           ├── setting/          # 设置页面
│           └── widget/           # 自定义控件 (ProgressDialog)
│
├── Bcore/                        # 核心脱壳库模块 (Java + Native)
│   ├── build.gradle              # Android Library, 含 CMake 配置
│   ├── black-hook/               # JNI Hook 子模块
│   │   └── src/main/java/top/niunaijun/jnihook/
│   │       ├── MethodUtils.java  # JNI 方法签名描述符工具
│   │       └── jni/ArtMethod.java # ART 方法结构体操作
│   ├── black-fake/               # 虚拟环境反射子模块
│   │   └── src/main/java/
│   │       ├── android/content/pm/  # PackageParser 等框架类 stub
│   │       └── reflection/          # 系统框架反射 (ActivityThread, ServiceManager 等)
│   └── src/main/
│       ├── java/top/niunaijun/blackbox/
│       │   ├── BlackBoxCore.java    # 虚拟环境核心 (VirtualApp 改造)
│       │   ├── BlackDexCore.java    # 脱壳入口, 封装安装+启动+Dump流程
│       │   ├── core/
│       │   │   ├── VMCore.java      # Native 接口桥接 (加载 libblackdex.so)
│       │   │   ├── IOCore.java      # IO 路径重定向
│       │   │   └── system/          # 系统服务模拟 (AM, PM, User 等)
│       │   ├── entity/
│       │   │   └── dump/DumpResult.java  # Dump 结果实体
│       │   └── fake/                # 系统服务 Fake 实现
│       ├── cpp/                     # Native 层 (C/C++)
│       │   ├── VmCore.cpp/.h        # JNI 入口, 注册 Native 方法
│       │   ├── DexDump.cpp/.h       # DEX Dump 核心实现
│       │   ├── IO.cpp/.h            # IO 重定向实现
│       │   ├── Dobby/               # Dobby inline hook 引擎 (子模块)
│       │   ├── xhook/               # xHook PLT hook 库
│       │   ├── jnihook/             # 自定义 JNI Hook
│       │   ├── hook/                # 各类 Hook 实现 (Process, VMClassLoader, UnixFileSystem)
│       │   ├── dex/                 # DEX 文件解析工具
│       │   ├── ziparchive/          # ZIP 归档处理
│       │   └── utils/               # Native 工具函数
│       └── aidl/                    # 系统服务 AIDL 接口定义
│
├── build.gradle                  # 根构建脚本
├── settings.gradle               # 模块声明: app, Bcore, Bcore:black-hook, Bcore:black-fake
└── gradle.properties             # AndroidX 启用, JVM 2048m
```

## 构建说明

1. 使用 Android Studio 打开项目根目录
2. **编译前必须先执行一次 Make Project** (Build -> Make Project), 否则 Native 层 SO 库不会生成
3. 选择构建变体:
   - `BlackDex32` - 32位 (armeabi-v7a), 包名 `top.niunaijun.blackdexa32`
   - `BlackDex64` - 64位 (arm64-v8a), 包名 `top.niunaijun.blackdexa64`
4. NDK 版本要求: 21.4.7075529
5. Native 库构建: CMake -> `Bcore/src/main/cpp/CMakeLists.txt` -> 输出 `libblackdex.so`

**注意事项**:
- 32位和64位是两个独立的 APK, 如果在已安装列表中找不到目标应用, 请切换另一个版本
- Debug 构建开启 `debuggable` 和 `jniDebuggable`
- 不启用混淆 (minifyEnabled false)

## 脱壳原理

### 核心流程

```
安装目标APK到虚拟环境 -> 启动目标应用 -> ART加载DEX -> Native Hook拦截 -> 导出DEX文件
```

1. **虚拟环境构建**: 基于 VirtualApp 改造的 BlackBoxCore, 在应用进程内创建独立的虚拟运行环境, 支持最多 100 个虚拟进程 (:p0 ~ :p99)
2. **APK 安装**: 将目标 APK 安装到虚拟环境的私有目录, 通过 IO 重定向使目标应用认为自己在正常环境中运行
3. **DEX 加载拦截**: 当目标应用启动时, ART 虚拟机通过 `DexFile` 加载 DEX, Native 层通过以下两种方式拦截:
   - **Cookie Dump**: 利用 `DexFile` 内部的 cookie 值, 直接从内存中读取并导出 DEX 文件
   - **Hook Dump**: Hook 系统 API (如 `openDexFile` 等), 在 DEX 加载时获取文件路径并复制
4. **深度脱壳**: 开启后会自动修复被指令抽取的 CodeItem, 将指向其他内存块的指令回填至 DEX 内, 解决 NOP 问题

### 关键 Native 接口 (VMCore.java)

| 方法 | 说明 |
|------|------|
| `init(int apiLevel)` | 初始化 Native 层, 设置 API Level |
| `enableIO()` | 启用 IO 路径重定向 |
| `addIORule(target, relocate)` | 添加 IO 重定向规则 |
| `hideXposed()` | 隐藏 Xposed 框架特征 |
| `cookieDumpDex(cookie, dir, fix)` | 通过 DexFile cookie Dump DEX |
| `hookDumpDex(dir)` | 通过 Hook 系统 API Dump DEX |

### 脱壳产物

| 文件 | 说明 |
|------|------|
| `cookie_xxxx.dex` | Cookie 方式脱壳的 DEX, 深度脱壳时会修复 CodeItem |
| `hook_xxxx.dex` | Hook 系统 API 脱壳的 DEX, 深度脱壳不修复 |

## 使用方式

1. 安装 BlackDex32 或 BlackDex64 到手机/模拟器
2. 打开应用, 授予存储权限
3. **已安装应用**: 在列表中选择目标应用, 点击脱壳
4. **未安装应用**: 通过文件选择器选择 APK 文件进行脱壳
5. 脱壳结果保存在应用私有目录的 `dex_dump/<包名>/` 下
6. 可在设置中开启"深度脱壳"以修复抽取指令 (耗时较长)

## 开发注意事项

- Bcore 模块的 Native 代码修改后, 需要重新执行 Make Project 编译 SO 库
- `BlackDexCore.java` 是脱壳流程的入口, 理解 `dumpDex()` -> `installPackage()` -> `launchApk()` 的调用链是关键
- `DexDump.cpp` 是 Native 层 Dump 的核心实现, 涉及内存读取和 DEX 文件头修复
- `DexFileCompat` 类负责从 ClassLoader 中提取 DexFile 的 cookie 值, 是连接 Java 层和 Native 层的桥梁
- `black-fake` 模块提供系统框架类的反射 Stub, 用于在虚拟环境中调用隐藏 API
- `black-hook` 模块的 `MethodUtils` 提供 JNI 方法签名描述符, 用于 ART Method Hook 时精确定位方法
