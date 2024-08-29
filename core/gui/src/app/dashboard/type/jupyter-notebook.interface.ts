export interface JupyterNotebook {
  cells: JupyterCell[];
  metadata: any;
  nbformat: number;
  nbformat_minor: number;
}

export interface JupyterCell {
  cell_type: string;
  source: string[];
  metadata: any;
  outputs?: JupyterOutput[];
}

export interface JupyterOutput {
  output_type: string;
  text?: string[];
  data?: { [key: string]: any };
  traceback?: string[];
}
