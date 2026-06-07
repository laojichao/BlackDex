package top.niunaijun.blackbox.utils.compat;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.util.DisplayMetrics;
import java.io.File;

import reflection.android.content.pm.PackageParserLollipop;
import reflection.android.content.pm.PackageParserLollipop22;
import reflection.android.content.pm.PackageParserMarshmallow;
import reflection.android.content.pm.PackageParserNougat;
import reflection.android.content.pm.PackageParserPie;

/**
 * PackageParser兼容性工具类。
 * <p>
 * 处理不同Android版本中 {@link PackageParser} 构造方法和API的变化：
 * <ul>
 *   <li>Android 5.0 (Lollipop)：PackageParser构造函数接受filePath参数</li>
 *   <li>Android 5.1 (Lollipop MR1)：构造函数改为无参，parsePackage增加flags参数</li>
 *   <li>Android 6.0 (Marshmallow)：进一步简化API</li>
 *   <li>Android 7.0 (Nougat)：collectCertificates签名变化</li>
 *   <li>Android 9.0 (Pie)：collectCertificates变为静态方法，增加skipVerify参数</li>
 * </ul>
 */
public class PackageParserCompat {

    /**
     * 创建与当前系统版本兼容的PackageParser实例。
     *
     * @param packageFile APK文件
     * @return PackageParser实例
     */
    public static PackageParser createParser(File packageFile) {
        if (BuildCompat.isM()) {
            return PackageParserMarshmallow.constructor.newInstance();
        } else if (BuildCompat.isL_MR1()) {
            return PackageParserLollipop22.constructor.newInstance();
        } else if (BuildCompat.isL()) {
            return PackageParserLollipop.constructor.newInstance();
        } else {
            return reflection.android.content.pm.PackageParser.constructor.newInstance(packageFile.getAbsolutePath());
        }
    }

    /**
     * 解析APK包信息。
     *
     * @param parser      PackageParser实例
     * @param packageFile APK文件
     * @param flags       解析标志位
     * @return 解析后的Package对象
     * @throws Throwable 解析失败时抛出
     */
    public static Package parsePackage(PackageParser parser, File packageFile, int flags) throws Throwable {
        if (BuildCompat.isM()) {
            return PackageParserMarshmallow.parsePackage.callWithException(parser, packageFile, flags);
        } else if (BuildCompat.isL_MR1()) {
            return PackageParserLollipop22.parsePackage.callWithException(parser, packageFile, flags);
        } else if (BuildCompat.isL()) {
            return PackageParserLollipop.parsePackage.callWithException(parser, packageFile, flags);
        } else {
            return reflection.android.content.pm.PackageParser.parsePackage.callWithException(parser, packageFile, null,
                    new DisplayMetrics(), flags);
        }
    }

    /**
     * 收集APK包的签名证书。
     *
     * @param parser PackageParser实例
     * @param p      Package对象
     * @param flags  收集标志位
     * @throws Throwable 收集失败时抛出
     */
    public static void collectCertificates(PackageParser parser, Package p, int flags) throws Throwable {
        if (BuildCompat.isPie()) {
            PackageParserPie.collectCertificates.callWithException(p, true/*skipVerify*/);
        } else if (BuildCompat.isN()) {
            PackageParserNougat.collectCertificates.callWithException(p, flags);
        } else if (BuildCompat.isM()) {
            PackageParserMarshmallow.collectCertificates.callWithException(parser, p, flags);
        } else if (BuildCompat.isL_MR1()) {
            PackageParserLollipop22.collectCertificates.callWithException(parser, p, flags);
        } else if (BuildCompat.isL()) {
            PackageParserLollipop.collectCertificates.callWithException(parser, p, flags);
        } else {
            reflection.android.content.pm.PackageParser.collectCertificates.call(parser, p, flags);
        }
    }
}
