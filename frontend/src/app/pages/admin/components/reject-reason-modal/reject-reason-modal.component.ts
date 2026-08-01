import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reject-reason-modal',
  imports: [CommonModule, FormsModule],
  templateUrl: './reject-reason-modal.component.html',
  styleUrl: './reject-reason-modal.component.scss'
})
export class RejectReasonModalComponent {
  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<string>();

  reason: string = '';

  onClose() {
    this.close.emit();
  }

  onConfirm() {
    if (this.reason.trim()) {
      this.confirm.emit(this.reason);
    }
  }
}
