export interface ConfirmOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  actionType?: 'primary' | 'danger' | 'info';
  icon?: string;
}

export interface ConfirmState {
  isOpen: boolean;
  options: ConfirmOptions;
}
