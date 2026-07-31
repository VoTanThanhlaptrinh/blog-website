import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmService } from '../../../core/services/confirm.service';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-modal.component.html'
})
export class ConfirmModalComponent {
  protected readonly confirmService = inject(ConfirmService);

  onConfirm(): void {
    this.confirmService.respond(true);
  }

  onCancel(): void {
    this.confirmService.respond(false);
  }
}
