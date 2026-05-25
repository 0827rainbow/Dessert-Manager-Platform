export interface TableDataInfo {
  code: number;
  msg: string;
  rows: any[];
  total: number;
  timestamp: number;
}

export interface ResponseResult {
  code: number;
  message: string;
  data?: any;
}
