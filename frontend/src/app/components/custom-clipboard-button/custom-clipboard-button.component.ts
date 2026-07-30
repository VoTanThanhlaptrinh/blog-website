import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClipboardButtonComponent } from 'ngx-markdown';

@Component({
  selector: 'app-custom-clipboard-button',
  imports: [CommonModule],
  templateUrl: './custom-clipboard-button.component.html',
  styleUrl: './custom-clipboard-button.component.scss',
})
export class CustomClipboardButtonComponent extends ClipboardButtonComponent {}
