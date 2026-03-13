/**
 * useOrderStatus —— 通用订单状态 composable
 *
 * 用法示例（销售订单）：
 *   const { statusList, getLabel, getTagType, getAllowedNextStatuses, changeStatus } =
 *     useOrderStatus('sales')
 *
 * 用法示例（采购订单）：
 *   const { statusList, getLabel, getTagType, changeStatus } =
 *     useOrderStatus('purchase')
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    getSalesOrderStatusMeta,
    getPurchaseOrderStatusMeta,
    updateSalesOrderStatus,
    updatePurchaseOrderStatus
} from '@/api/orderStatus'

export interface StatusMeta {
    code: number
    label: string
    tagType: string
    allowedFromCodes: number[]
}

export function useOrderStatus(type: 'sales' | 'purchase') {
    const statusList = ref<StatusMeta[]>([])

    // 从后端加载状态元数据
    async function loadStatusMeta() {
        const res = type === 'sales'
            ? await getSalesOrderStatusMeta()
            : await getPurchaseOrderStatusMeta()
        statusList.value = res.data as StatusMeta[]
    }

    onMounted(loadStatusMeta)

    /** 根据状态码获取显示文本 */
    function getLabel(code: number): string {
        return statusList.value.find(s => s.code === code)?.label ?? String(code)
    }

    /** 根据状态码获取 Element Plus tag type */
    function getTagType(code: number): string {
        return statusList.value.find(s => s.code === code)?.tagType ?? ''
    }

    /**
     * 获取当前状态允许流转到的下一步状态列表
     * （即 allowedFromCodes 中包含 currentCode 的所有状态）
     */
    function getAllowedNextStatuses(currentCode: number): StatusMeta[] {
        return statusList.value.filter(s => s.allowedFromCodes.includes(currentCode))
    }

    /**
     * 执行状态流转：弹确认框 → 调接口 → 刷新列表
     * @param id         订单 ID
     * @param targetCode 目标状态码
     * @param onSuccess  成功回调（一般用于刷新列表）
     */
    async function changeStatus(id: number, targetCode: number, onSuccess?: () => void) {
        const targetLabel = getLabel(targetCode)
        try {
            await ElMessageBox.confirm(
                `确认将状态变更为「${targetLabel}」？`,
                '状态变更',
                { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' }
            )
        } catch {
            return // 用户取消
        }

        try {
            if (type === 'sales') {
                await updateSalesOrderStatus(id, targetCode)
            } else {
                await updatePurchaseOrderStatus(id, targetCode)
            }
            ElMessage.success(`已变更为「${targetLabel}」`)
            onSuccess?.()
        } catch {
            // 错误已由 request.ts 拦截器通过 ElMessage 展示，无需重复处理
        }
    }

    return { statusList, loadStatusMeta, getLabel, getTagType, getAllowedNextStatuses, changeStatus }
}
