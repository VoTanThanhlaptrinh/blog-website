import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  imports: [RouterLink],
  templateUrl: './footer.component.html',
})
export class FooterComponent {
  readonly year = new Date().getFullYear();

  readonly columns = [
    {
      title: 'Khám phá',
      links: [
        { label: 'Trang chủ', path: '/' },
        { label: 'Bài viết', path: '/search' },
        { label: 'Chủ đề', path: '/search' },
      ],
    },
    {
      title: 'Cộng đồng',
      links: [
        { label: 'Viết bài', path: '/blog/creation' },
        { label: 'Tác giả', path: '/search' },
        { label: 'Thảo luận', path: '/search' },
      ],
    },
    {
      title: 'Hỗ trợ',
      links: [
        { label: 'Giới thiệu', path: '/about' },
        { label: 'Liên hệ', path: '/contact' },
        { label: 'Điều khoản', path: '/terms' },
      ],
    },
  ];
}
