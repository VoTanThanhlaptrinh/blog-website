import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InteractionService } from '../../../core/services/interaction.service';
import { BlogResponse } from '../../../core/models/blog.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-profile-saved',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile-saved.component.html'
})
export class ProfileSavedComponent implements OnInit {
  private readonly interactionService = inject(InteractionService);

  readonly blogs = signal<BlogResponse[]>([]);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadSaved();
  }

  loadSaved(): void {
    this.loading.set(true);
    this.interactionService.getMyBookmarks(0, 20)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe((page) => {
        this.blogs.set(page.content || []);
      });
  }

  removeBookmark(blogId: number, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.interactionService.toggleBookmark(blogId).subscribe(() => {
      this.blogs.set(this.blogs().filter(b => b.id !== blogId));
    });
  }
}
