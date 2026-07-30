import { Component, Input, Output, EventEmitter, signal, forwardRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommentResponse } from '../../core/models/interaction.model';

@Component({
  selector: 'app-comment-item',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, forwardRef(() => CommentItemComponent)],
  templateUrl: './comment-item.component.html',
})
export class CommentItemComponent {
  @Input({ required: true }) comment!: CommentResponse;
  @Input() currentUserId?: number;
  @Input() level = 0;

  @Output() likeComment = new EventEmitter<number>();
  @Output() replyComment = new EventEmitter<{ parentId: number; authorEmail: string }>();
  @Output() editComment = new EventEmitter<{ commentId: number; content: string }>();
  @Output() deleteComment = new EventEmitter<number>();

  isEditing = signal(false);
  editContent = signal('');

  toggleEdit() {
    if (!this.isEditing()) {
      this.editContent.set(this.comment.content);
      this.isEditing.set(true);
    } else {
      this.isEditing.set(false);
    }
  }

  saveEdit() {
    const trimmed = this.editContent().trim();
    if (trimmed && trimmed !== this.comment.content) {
      this.editComment.emit({ commentId: this.comment.id, content: trimmed });
    }
    this.isEditing.set(false);
  }

  onReplyClick() {
    this.replyComment.emit({
      parentId: this.comment.id,
      authorEmail: this.comment.author?.email || 'người dùng',
    });
  }

  onLike() {
    this.likeComment.emit(this.comment.id);
  }

  onDelete() {
    if (confirm('Bạn có chắc chắn muốn xóa bình luận này?')) {
      this.deleteComment.emit(this.comment.id);
    }
  }

  // Forward child events up the component tree
  onChildLike(commentId: number) {
    this.likeComment.emit(commentId);
  }

  onChildReply(event: { parentId: number; authorEmail: string }) {
    this.replyComment.emit(event);
  }

  onChildEdit(event: { commentId: number; content: string }) {
    this.editComment.emit(event);
  }

  onChildDelete(commentId: number) {
    this.deleteComment.emit(commentId);
  }
}
