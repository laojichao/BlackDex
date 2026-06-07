package top.niunaijun.blackdex.data.entity

/**
 * DEX dump 操作结果实体。
 *
 * @property state 操作状态码，取值为 [SUCCESS] / [FAIL] / [LOADING] / [TIMEOUT]
 * @property msg 附加信息（如 DEX 保存路径或错误描述）
 *
 * @author wukaicheng
 */
data class DumpInfo(
        val state: Int,
        val msg: String = ""
) {
    companion object {
        /** dump 成功 */
        const val SUCCESS = 200

        /** dump 失败 */
        const val FAIL = 404

        /** dump 进行中（加载状态） */
        const val LOADING = 300

        /** dump 超时 */
        const val TIMEOUT = 500
    }
}