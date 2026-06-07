package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.jar.Attributes;

/**
 * Android 隐藏 API {@code android.content.pm.ManifestDigest} 的桩类。
 * <p>
 * 用于对 APK 的 AndroidManifest.xml 进行摘要校验，实现 {@link Parcelable} 接口。
 * 该类在编译期作为桩存在，运行时由框架提供真实实现。
 */
public class ManifestDigest implements Parcelable {

    /**
     * 使用摘要字节数组构造 ManifestDigest 实例。
     *
     * @param digest Manifest 文件的摘要字节数组
     */
    ManifestDigest(final byte[] digest) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 从 Parcel 反序列化构造 ManifestDigest 实例。
     *
     * @param source 包含序列化数据的 Parcel 对象
     */
    private ManifestDigest(final Parcel source) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 从 JAR Manifest 属性中提取并构造 ManifestDigest 实例。
     *
     * @param attributes JAR 文件的 Manifest 属性
     * @return 新构造的 ManifestDigest 实例
     */
    static ManifestDigest fromAttributes(final Attributes attributes) {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        throw new RuntimeException("Stub!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Parcelable 反序列化创建器。
     */
    public static final Parcelable.Creator<ManifestDigest> CREATOR = new Parcelable.Creator<ManifestDigest>() {
        public ManifestDigest createFromParcel(Parcel source) {
            return new ManifestDigest(source);
        }

        public ManifestDigest[] newArray(int size) {
            return new ManifestDigest[size];
        }
    };

}