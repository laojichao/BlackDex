package android.content.pm;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Android 隐藏 API {@code android.content.pm.PackageParser} 的桩类。
 * <p>
 * 用于解析 APK 文件并提取包信息，包括组件、权限、签名等。
 * 该桩类包含 PackageParser 在不同 Android 版本中使用的核心内部类（Package、Activity、Service、Provider 等），
 * 供 BlackDex 在编译期引用，运行时由框架提供真实实现。
 */
public class PackageParser {

    /** 解析标志：系统应用 */
    public final static int PARSE_IS_SYSTEM = 1 << 0;
    /** 解析标志：详细日志输出 */
    public final static int PARSE_CHATTY = 1 << 1;
    /** 解析标志：必须是 APK 文件 */
    public final static int PARSE_MUST_BE_APK = 1 << 2;
    /** 解析标志：忽略进程信息 */
    public final static int PARSE_IGNORE_PROCESSES = 1 << 3;
    /** 解析标志：前向锁定 */
    public final static int PARSE_FORWARD_LOCK = 1 << 4;
    /** 解析标志：安装在外部存储 */
    public final static int PARSE_EXTERNAL_STORAGE = 1 << 5;
    /** 解析标志：系统目录中的应用 */
    public final static int PARSE_IS_SYSTEM_DIR = 1 << 6;
    /** 解析标志：特权应用 */
    public final static int PARSE_IS_PRIVILEGED = 1 << 7;
    /** 解析标志：收集证书信息 */
    public final static int PARSE_COLLECT_CERTIFICATES = 1 << 8;
    /** 解析标志：可信覆盖层 */
    public final static int PARSE_TRUSTED_OVERLAY = 1 << 9;

    /**
     * 新增权限信息，记录在特定 SDK 版本引入的权限。
     */
    public static class NewPermissionInfo {
        public final String name;
        public final int sdkVersion;
        public final int fileVersion;

        public NewPermissionInfo(String name, int sdkVersion, int fileVersion) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 权限拆分信息，记录某个根权限在特定 SDK 版本后拆分出的子权限。
     */
    public static class SplitPermissionInfo {
        public final String rootPerm;
        public final String[] newPerms;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm, String[] newPerms, int targetSdk) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 新增权限列表，记录从 DONUT 版本开始引入的权限。
     */
    public static final PackageParser.NewPermissionInfo NEW_PERMISSIONS[] = new PackageParser.NewPermissionInfo[]{
            new PackageParser.NewPermissionInfo(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.os.Build.VERSION_CODES.DONUT, 0),
            new PackageParser.NewPermissionInfo(android.Manifest.permission.READ_PHONE_STATE, android.os.Build.VERSION_CODES.DONUT, 0)
    };

    /**
     * 解析包项目参数的基类，包含解析所需的资源 ID 和标签信息。
     */
    static class ParsePackageItemArgs {
        final Package owner;
        final String[] outError;
        final int nameRes;
        final int labelRes;
        final int iconRes;
        final int logoRes;
        final int bannerRes;

        String tag;
        TypedArray sa;

        ParsePackageItemArgs(final Package owner, final String[] outError, final int nameRes, final int labelRes, final int iconRes, final int logoRes, final int bannerRes) { throw new RuntimeException("Stub!");
        }
    }

    /**
     * 解析组件参数，继承自 {@link ParsePackageItemArgs}，额外包含进程、描述和启用状态等资源 ID。
     */
    static class ParseComponentArgs extends ParsePackageItemArgs {
        final String[] sepProcesses;
        final int processRes;
        final int descriptionRes;
        final int enabledRes;
        int flags;

        ParseComponentArgs(final Package owner, final String[] outError, final int nameRes, final int labelRes, final int iconRes, final int logoRes, final int bannerRes, final String[] sepProcesses, final int processRes, final int descriptionRes, final int enabledRes) {
            super(owner, outError, nameRes, labelRes, iconRes, logoRes, bannerRes);
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 轻量级包信息，仅包含包名、版本和安装位置等基本信息，不解析完整组件。
     */
    public static class PackageLite {
        public final String packageName;
        public final int versionCode;
        public final int installLocation;
        public final VerifierInfo[] verifiers;

        /** Names of any split APKs, ordered by parsed splitName */
        public final String[] splitNames;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        public final String codePath;

        /** Path of base APK */
        public final String baseCodePath;
        /** Paths of any split APKs, ordered by parsed splitName */
        public final String[] splitCodePaths;

        /** Revision code of base APK */
        public final int baseRevisionCode;
        /** Revision codes of any split APKs, ordered by parsed splitName */
        public final int[] splitRevisionCodes;

        public final boolean coreApp;
        public final boolean multiArch;
        public final boolean extractNativeLibs;

        public PackageLite(final String codePath, final ApkLite baseApk, final String[] splitNames, final String[] splitCodePaths, final int[] splitRevisionCodes) {
            throw new RuntimeException("Stub!");
        }

        public List<String> getAllCodePaths() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 单个 APK 文件的轻量级信息，包含包名、版本、签名等基本信息。
     */
    public static class ApkLite {
        public final String codePath;
        public final String packageName;
        public final String splitName;
        public final int versionCode;
        public final int revisionCode;
        public final int installLocation;
        public final VerifierInfo[] verifiers;
        public final Signature[] signatures;
        public final boolean coreApp;
        public final boolean multiArch;
        public final boolean extractNativeLibs;

        public ApkLite(final String codePath, final String packageName, final String splitName, final int versionCode, final int revisionCode, final int installLocation, final List<VerifierInfo> verifiers, final Signature[] signatures, final boolean coreApp, final boolean multiArch, final boolean extractNativeLibs) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * For Android 5.0+
     */
    public PackageParser() {
        throw new RuntimeException("Stub!");
    }

    public PackageParser(final String archiveSourcePath) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 设置需要独立进程运行的进程名列表。
     *
     * @param procs 需要独立进程的进程名数组
     */
    public void setSeparateProcesses(final String[] procs) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 设置是否仅解析核心应用。
     *
     * @param onlyCoreApps true 表示仅解析核心应用
     */
    public void setOnlyCoreApps(final boolean onlyCoreApps) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 设置显示度量信息，用于解析资源时的密度适配。
     *
     * @param metrics 显示度量信息
     */
    public void setDisplayMetrics(final DisplayMetrics metrics) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 判断指定文件是否为 APK 文件。
     *
     * @param file 待检查的文件
     * @return 如果是 APK 文件返回 true
     */
    public static final boolean isApkFile(final File file) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 根据解析后的 Package 对象生成 PackageInfo。
     *
     * @param p 解析后的 Package 对象
     * @param gids 组 ID 数组
     * @param flags 标志位
     * @param firstInstallTime 首次安装时间
     * @param lastUpdateTime 最后更新时间
     * @param grantedPermissions 已授权的权限集合
     * @param state 用户包状态
     * @return 生成的 PackageInfo
     */
    public static PackageInfo generatePackageInfo(final PackageParser.Package p, final int gids[], final int flags, final long firstInstallTime, final long lastUpdateTime, final Set<String> grantedPermissions, final PackageUserState state) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 根据用户包状态判断应用是否可用。
     *
     * @param state 用户包状态
     * @return 如果应用可用返回 true
     */
    public static boolean isAvailable(final PackageUserState state) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 根据解析后的 Package 对象生成 PackageInfo（Android 7.0+ 版本，带 userId 参数）。
     *
     * @param p 解析后的 Package 对象
     * @param gids 组 ID 数组
     * @param flags 标志位
     * @param firstInstallTime 首次安装时间
     * @param lastUpdateTime 最后更新时间
     * @param grantedPermissions 已授权的权限集合
     * @param state 用户包状态
     * @param userId 用户 ID
     * @return 生成的 PackageInfo
     */
    public static PackageInfo generatePackageInfo(final PackageParser.Package p, final int gids[], final int flags, final long firstInstallTime, final long lastUpdateTime, final Set<String> grantedPermissions, final PackageUserState state, final int userId) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parse only lightweight details about the package at the given location.
     * Automatically detects if the package is a monolithic style (single APK
     * file) or cluster style (directory of APKs).
     * <p>
     * This performs sanity checking on cluster style packages, such as
     * requiring identical package name and version codes, a single base APK,
     * and unique split names.
     *
     * @see PackageParser#parsePackage(File, int)
     */
    public static PackageLite parsePackageLite(final File packageFile, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parse the package at the given location. Automatically detects if the
     * package is a monolithic style (single APK file) or cluster style
     * (directory of APKs).
     * <p>
     * This performs sanity checking on cluster style packages, such as
     * requiring identical package name and version codes, a single base APK,
     * and unique split names.
     * <p>
     * Note that this <em>does not</em> perform signature verification; that
     * must be done separately in {@link #collectCertificates(Package, int)}.
     *
     * @see #parsePackageLite(File, int)
     * @since Android 5.0+
     */
    public Package parsePackage(final File packageFile, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     *
     * @param sourceFile
     * @param destCodePath
     * @param metrics
     * @param flags
     * @return
     * @since Android 2.3+
     */
    public Package parsePackage(final File sourceFile, final String destCodePath, final DisplayMetrics metrics, final int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 收集包的 Manifest 摘要信息。
     *
     * @param pkg 解析后的 Package 对象
     * @throws PackageParserException 解析异常
     */
    public void collectManifestDigest(final Package pkg) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * 收集包的签名证书信息。
     *
     * @param pkg 解析后的 Package 对象
     * @param flags 解析标志
     * @throws PackageParserException 解析异常
     */
    public void collectCertificates(final Package pkg, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Utility method that retrieves lightweight details about a single APK
     * file, including package name, split name, and install location.
     *
     * @param apkFile path to a single APK
     * @param flags optional parse flags, such as
     *            {@link #PARSE_COLLECT_CERTIFICATES}
     */
    public static ApkLite parseApkLite(final File apkFile, final int flags) throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    /**
     * Representation of a full package parsed from APK files on disk. A package
     * consists of a single base APK, and zero or more split APKs.
     */
    public final static class Package {

        public String packageName;

        /** Names of any split APKs, ordered by parsed splitName */
        public String[] splitNames;

        // TODO: work towards making these paths invariant

        public String volumeUuid;

        /**
         * Path where this package was found on disk. For monolithic packages
         * this is path to single base APK file; for cluster packages this is
         * path to the cluster directory.
         */
        public String codePath;

        /** Path of base APK */
        public String baseCodePath;
        /** Paths of any split APKs, ordered by parsed splitName */
        public String[] splitCodePaths;

        /** Revision code of base APK */
        public int baseRevisionCode;
        /** Revision codes of any split APKs, ordered by parsed splitName */
        public int[] splitRevisionCodes;

        /** Flags of any split APKs; ordered by parsed splitName */
        public int[] splitFlags;

        /**
         * Private flags of any split APKs; ordered by parsed splitName.
         *
         * {@hide}
         */
        public int[] splitPrivateFlags;

        public boolean baseHardwareAccelerated;

        // For now we only support one application per package.
        public final ApplicationInfo applicationInfo = new ApplicationInfo();

        public final ArrayList<Permission> permissions = new ArrayList<Permission>(0);
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>(0);
        public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
        public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
        public final ArrayList<Provider> providers = new ArrayList<Provider>(0);
        public final ArrayList<Service> services = new ArrayList<Service>(0);
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<Instrumentation>(0);

        public final ArrayList<String> requestedPermissions = new ArrayList<String>();

        public ArrayList<String> protectedBroadcasts;

        public ArrayList<String> libraryNames = null;
        public ArrayList<String> usesLibraries = null;
        public ArrayList<String> usesOptionalLibraries = null;
        public String[] usesLibraryFiles = null;

        public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;

        public ArrayList<String> mOriginalPackages = null;
        public String mRealPackage = null;
        public ArrayList<String> mAdoptPermissions = null;

        // We store the application meta-data independently to avoid multiple unwanted references
        public Bundle mAppMetaData = null;

        // The version code declared for this package.
        public int mVersionCode;

        // The version name declared for this package.
        public String mVersionName;

        // The shared user id that this package wants to use.
        public String mSharedUserId;

        // The shared user label that this package wants to use.
        public int mSharedUserLabel;

        // Signatures that were read from the package.
        public Signature[] mSignatures;
        public SigningDetails mSigningDetails;
        public Certificate[][] mCertificates;

        // For use by package manager service for quick lookup of
        // preferred up order.
        public int mPreferredOrder = 0;

        // For use by package manager to keep track of where it needs to do dexopt.
//        public final ArraySet<String> mDexOptPerformed = new ArraySet<>(4);

        // For use by package manager to keep track of when a package was last used.
        public long mLastPackageUsageTimeInMills;

        // // User set enabled state.
        // public int mSetEnabled = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        //
        // // Whether the package has been stopped.
        // public boolean mSetStopped = false;

        // Additional data supplied by callers.
        public Object mExtras;

        // Applications hardware preferences
        public ArrayList<ConfigurationInfo> configPreferences = null;

        // Applications requested features
        public ArrayList<FeatureInfo> reqFeatures = null;

        // Applications requested feature groups
        public ArrayList<FeatureGroupInfo> featureGroups = null;

        public int installLocation;

        public boolean coreApp;

        /* An app that's required for all users and cannot be uninstalled for a user */
        public boolean mRequiredForAllUsers;

        /* The restricted account authenticator type that is used by this application */
        public String mRestrictedAccountType;

        /* The required account type without which this application will not function */
        public String mRequiredAccountType;

        /**
         * Digest suitable for comparing whether this package's manifest is the
         * same as another.
         */
        public ManifestDigest manifestDigest;

        public String mOverlayTarget;
        public int mOverlayPriority;
        public boolean mTrustedOverlay;

        /**
         * Data used to feed the KeySetManagerService
         */
        public ArraySet<PublicKey> mSigningKeys;
        public ArraySet<String> mUpgradeKeySets;
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;

        /**
         * The install time abi override for this package, if any.
         *
         * TODO: This seems like a horrible place to put the abiOverride because
         * this isn't something the packageParser parsers. However, this fits in with
         * the rest of the PackageManager where package scanning randomly pushes
         * and prods fields out of {@code this.applicationInfo}.
         */
        public String cpuAbiOverride;

        public Package(String packageName) {
            throw new RuntimeException("Stub!");
        }

        public List<String> getAllCodePaths() {
            throw new RuntimeException("Stub!");
        }

        /**
         * Filtered set of {@link #getAllCodePaths()} that excludes
         * resource-only APKs.
         */
        public List<String> getAllCodePathsExcludingResourceOnly() {
            throw new RuntimeException("Stub!");
        }

        public void setPackageName(final String newName) {
            throw new RuntimeException("Stub!");
        }

        public boolean hasComponentClassName(final String name) {
            throw new RuntimeException("Stub!");
        }

        public boolean isForwardLocked() {
            throw new RuntimeException("Stub!");
        }

        public boolean isSystemApp() {
            throw new RuntimeException("Stub!");
        }

        public boolean isPrivilegedApp() {
            throw new RuntimeException("Stub!");
        }

        public boolean isUpdatedSystemApp() {
            throw new RuntimeException("Stub!");
        }

        public boolean canHaveOatDir() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 通用组件基类，表示包中的一个组件（Activity、Service、Provider、Permission 等）。
     *
     * @param <II> IntentInfo 的子类型
     */
    public static class Component<II extends IntentInfo> {
        public final Package owner;
        public final ArrayList<II> intents;
        public final String className;
        public Bundle metaData;

        ComponentName componentName;
        String componentShortName;

        public Component(final Package owner) {
            throw new RuntimeException("Stub!");
        }

        public Component(final ParsePackageItemArgs args, final PackageItemInfo outInfo) {
            throw new RuntimeException("Stub!");
        }

        public Component(final ParseComponentArgs args, final ComponentInfo outInfo) {
            throw new RuntimeException("Stub!");
        }

        public Component(final Component<II> clone) {
            throw new RuntimeException("Stub!");
        }

        public ComponentName getComponentName() {
            throw new RuntimeException("Stub!");
        }

        public void appendComponentShortName(final StringBuilder sb) {
            throw new RuntimeException("Stub!");
        }

        public void printComponentShortName(final PrintWriter pw) {
            throw new RuntimeException("Stub!");
        }

        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 权限组件，表示在 Manifest 中声明的权限。
     */
    public final static class Permission extends Component<IntentInfo> {
        public final PermissionInfo info;
        public boolean tree;
        public PermissionGroup group;

        public Permission(final Package owner) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        public Permission(final Package owner, final PermissionInfo info) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 权限组组件，表示在 Manifest 中声明的权限组。
     */
    public final static class PermissionGroup extends Component<IntentInfo> {
        public final PermissionGroupInfo info;

        public PermissionGroup(final Package owner) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        public PermissionGroup(final Package owner, final PermissionGroupInfo info) {
            super(owner);
            throw new RuntimeException("Stub!");
        }

        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Activity 组件，表示在 Manifest 中声明的 Activity。
     */
    public final static class Activity extends Component<ActivityIntentInfo> {
        public final ActivityInfo info;

        public Activity(final ParseComponentArgs args, final ActivityInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Service 组件，表示在 Manifest 中声明的 Service。
     */
    public final static class Service extends Component<ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(final ParseComponentArgs args, final ServiceInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * ContentProvider 组件，表示在 Manifest 中声明的 ContentProvider。
     */
    public final static class Provider extends Component<ProviderIntentInfo> {
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(final ParseComponentArgs args, final ProviderInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        public Provider(final Provider existingProvider) {
            super(existingProvider);
            throw new RuntimeException("Stub!");
        }

        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Instrumentation 组件，表示在 Manifest 中声明的 Instrumentation。
     */
    public final static class Instrumentation extends Component<IntentInfo> {
        public final InstrumentationInfo info;

        public Instrumentation(final ParsePackageItemArgs args, final InstrumentationInfo info) {
            super(args, info);
            throw new RuntimeException("Stub!");
        }

        @Override
        public void setPackageName(final String packageName) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Intent 过滤器信息，继承自 {@link IntentFilter}，包含默认行为和标签等额外信息。
     */
    @SuppressLint("ParcelCreator")
    public static class IntentInfo extends IntentFilter {
        public boolean hasDefault;
        public int labelRes;
        public CharSequence nonLocalizedLabel;
        public int icon;
        public int logo;
        public int banner;
        public int preferred;
    }

    /**
     * Activity 的 Intent 过滤器信息。
     */
    @SuppressLint("ParcelCreator")
    public final static class ActivityIntentInfo extends IntentInfo {
        public final Activity activity;

        public ActivityIntentInfo(final Activity activity) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * Service 的 Intent 过滤器信息。
     */
    @SuppressLint("ParcelCreator")
    public final static class ServiceIntentInfo extends IntentInfo {
        public final Service service;

        public ServiceIntentInfo(final Service service) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * ContentProvider 的 Intent 过滤器信息。
     */
    @SuppressLint("ParcelCreator")
    public static final class ProviderIntentInfo extends IntentInfo {
        public final Provider provider;

        public ProviderIntentInfo(final Provider provider) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * 包解析异常，包含错误码和详细信息。
     */
    public static class PackageParserException extends Exception {

        public PackageParserException(int error, String detailMessage) {
            super(detailMessage);
            throw new RuntimeException("Stub!");
        }

        public PackageParserException(int error, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            throw new RuntimeException("Stub!");
        }

    }

    /**
     * 签名详情（Android 9.0+），包含签名数组和未知签名常量。
     */
    public static class SigningDetails {
        public static final SigningDetails UNKNOWN = null;
        public Signature[] signatures;
    }
}