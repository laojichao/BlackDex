package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.PublicKey;

/**
 * Android 隐藏 API {@code android.content.pm.VerifierInfo} 的桩类。
 * <p>
 * 表示 APK 验证器的信息，包含验证器的包名和公钥。
 * 实现 {@link Parcelable} 接口以支持序列化传输。
 */
public class VerifierInfo implements Parcelable {

    /**
     * Parcelable 反序列化创建器。
     */
    public static final Parcelable.Creator<VerifierInfo> CREATOR = new Parcelable.Creator<VerifierInfo>() {
        public VerifierInfo createFromParcel(final Parcel source) {
            return new VerifierInfo(source);
        }

        public VerifierInfo[] newArray(final int size) {
            return new VerifierInfo[size];
        }
    };

    /**
     * 使用包名和公钥构造 VerifierInfo 实例。
     *
     * @param packageName 验证器的包名
     * @param publicKey 验证器的公钥
     */
    public VerifierInfo(final String packageName, final PublicKey publicKey) {
        throw new RuntimeException("Stub!");
    }

    /**
     * 从 Parcel 反序列化构造 VerifierInfo 实例。
     *
     * @param source 包含序列化数据的 Parcel 对象
     */
    private VerifierInfo(final Parcel source) {
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
    public void writeToParcel(final Parcel dest, final int flags) {
        throw new RuntimeException("Stub!");
    }
}