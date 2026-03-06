import * as XLSX from 'xlsx'

type ExcelColumn<T> = {
  label: string
  key?: keyof T
  value?: (row: T) => unknown
}

export function exportToCsv<T extends Record<string, any>>(
  filename: string,
  rows: T[],
  columns: ExcelColumn<T>[]
) {
  const header = columns.map(c => c.label)
  const data = rows.map(row =>
    columns.map(col => {
      const v = col.value ? col.value(row) : col.key ? row[col.key] : ''
      return v === null || v === undefined ? '' : v
    })
  )

  const worksheet = XLSX.utils.aoa_to_sheet([header, ...data])
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1')

  const baseName = filename.replace(/\.(csv|xlsx)$/, '')
  XLSX.writeFile(workbook, `${baseName}.xlsx`)
}

