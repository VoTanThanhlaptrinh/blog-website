# Bộ công cụ soạn thảo Markdown — Giải thích cách hoạt động

Tài liệu này giải thích cách chức năng đăng bài hoạt động: từ luồng dữ liệu tổng thể, cơ chế các icon thao tác, đến cách nội dung được hiển thị. Nội dung theo hướng dạy cách hoạt động, không chỉ mô tả.

---

## Phần 1: Bức tranh tổng thể — dữ liệu chảy như thế nào

Trước khi đi vào từng nút, cần nắm mô hình cốt lõi. Toàn bộ editor xoay quanh **một biến string duy nhất**: `body`. Đây là "nguồn sự thật" (source of truth).

```
[Người dùng gõ / bấm nút]  →  thay đổi chuỗi `body`  →  [Preview render lại]
```

- Ô soạn thảo bên trái là một `<textarea>` bind hai chiều với `body` qua `[(ngModel)]="body"`.
- Cột preview bên phải nhận chính `body` đó và biến markdown thành HTML.

Điểm mấu chốt để hiểu mọi thứ: **các nút toolbar không "định dạng" gì cả**. Chúng chỉ chèn thêm ký tự markdown vào chuỗi `body`. Ví dụ bấm Bold không làm chữ đậm ngay — nó chỉ thêm `**` vào hai đầu đoạn text. Việc `**` biến thành chữ đậm là nhiệm vụ của bộ render ở cột phải. Tách bạch hai việc này là chìa khóa.

Đây gọi là kiến trúc **controlled component**: Angular kiểm soát giá trị, textarea chỉ là "cửa sổ" nhìn vào giá trị đó. Khi ta sửa `body` bằng code, Angular tự động cập nhật lại textarea.

---

## Phần 2: Các icon thao tác hoạt động ra sao

Có hai kiểu định dạng markdown, và có hai hàm nền tảng khác nhau cho chúng.

### 2.1. Định dạng "bao quanh" — Bold, Italic, Code, Link

Markdown kiểu này bọc text vào giữa hai ký hiệu: `**đậm**`, `*nghiêng*`, `` `code` ``. Hàm xử lý là `wrapSelection`:

```ts
private wrapSelection(prefix: string, suffix: string, placeholder: string): void {
  const el = this.bodyInput.nativeElement;
  const start = el.selectionStart;   // vị trí bắt đầu bôi đen
  const end = el.selectionEnd;       // vị trí kết thúc bôi đen
  const selected = this.body.slice(start, end) || placeholder;
  const before = this.body.slice(0, start);
  const after = this.body.slice(end);

  this.body = `${before}${prefix}${selected}${suffix}${after}`;
  // ...đặt lại con trỏ
}
```

Cơ chế từng bước:

1. **Lấy vùng bôi đen**: mỗi `<textarea>` DOM có sẵn hai thuộc tính `selectionStart` và `selectionEnd` — là chỉ số ký tự nơi con trỏ bắt đầu và kết thúc. Nếu không bôi đen gì, hai số này bằng nhau.
2. **Cắt chuỗi làm 3 khúc**: phần *trước* vùng chọn, phần *được chọn*, phần *sau*. Đây là kỹ thuật string slicing cơ bản nhưng là trái tim của cả tính năng.
3. **Ghép lại** với ký hiệu markdown chèn vào giữa: `before + ** + selected + ** + after`.
4. **Placeholder**: nếu chưa bôi đen gì (`selected` rỗng), ta chèn text mẫu như `"văn bản in đậm"` để người dùng thấy có gì đó xảy ra, thay vì chèn `****` trống trơn.

Để `bodyInput.nativeElement` chạy được, dùng `@ViewChild('bodyInput')` trong TS và gắn `#bodyInput` lên textarea trong HTML — đây là cách Angular cho code truy cập thẳng vào phần tử DOM thật.

Các handler cụ thể chỉ là lời gọi ngắn tới hàm chung:

```ts
bold()       { this.wrapSelection('**', '**', 'văn bản in đậm'); }
italic()     { this.wrapSelection('*', '*', 'văn bản in nghiêng'); }
inlineCode() { this.wrapSelection('`', '`', 'code'); }
link()       { this.wrapSelection('[', '](https://)', 'liên kết'); }
```

Chú ý Link: prefix là `[`, suffix là `](https://)`. Ghép lại thành `[text](https://)` — đúng cú pháp link markdown. Đây là lý do tách prefix/suffix riêng thay vì dùng một ký hiệu, để tái sử dụng được cho cả trường hợp bất đối xứng.

### 2.2. Định dạng "đầu dòng" — Heading, Quote, List

Nhóm này khác về bản chất: markdown của chúng nằm ở **đầu dòng**, không bọc quanh. `# Tiêu đề`, `> Trích dẫn`, `- Mục list`. Nếu dùng `wrapSelection` sẽ sai. Nên có hàm riêng `prefixLines`:

```ts
private prefixLines(linePrefix: string): void {
  const el = this.bodyInput.nativeElement;
  const start = el.selectionStart;
  const end = el.selectionEnd;

  const lineStart = this.body.lastIndexOf('\n', start - 1) + 1;  // lùi về đầu dòng
  const before = this.body.slice(0, lineStart);
  const block = this.body.slice(lineStart, end) || '';
  const after = this.body.slice(end);

  const transformed = block
    .split('\n')
    .map((line) => `${linePrefix}${line}`)   // thêm prefix vào MỖI dòng
    .join('\n');

  this.body = `${before}${transformed}${after}`;
  // ...
}
```

Hai điểm tinh tế đáng học:

- **`lastIndexOf('\n', start - 1) + 1`**: dù con trỏ đang ở giữa dòng, ta phải lùi về đầu dòng thì mới chèn `# ` đúng chỗ. `lastIndexOf` tìm ký tự xuống dòng gần nhất *trước* con trỏ; `+ 1` để nhảy tới ký tự ngay sau nó (tức đầu dòng). Nếu là dòng đầu tiên, `lastIndexOf` trả `-1`, cộng 1 thành `0` — vẫn đúng.
- **`.split('\n').map().join('\n')`**: nếu người dùng bôi đen nhiều dòng rồi bấm List, ta muốn *mỗi* dòng đều có `- `. Kỹ thuật tách–biến đổi–ghép này áp prefix lên từng dòng một.

### 2.3. Vấn đề con trỏ — tại sao có `setTimeout`

Khi ta gán `this.body = ...`, Angular cần một nhịp để cập nhật DOM. Nếu đặt lại con trỏ ngay lập tức, textarea chưa có nội dung mới, con trỏ sẽ nhảy sai. `restoreSelection` dùng `setTimeout` để hoãn việc này tới sau khi Angular vẽ xong:

```ts
private restoreSelection(start: number, end: number): void {
  setTimeout(() => {
    const el = this.bodyInput.nativeElement;
    el.focus();
    el.setSelectionRange(start, end);   // giữ nguyên vùng bôi đen sau khi chèn
  });
}
```

Nhờ vậy, sau khi bấm Bold, đoạn text vẫn được bôi đen (giờ nằm giữa hai `**`), người dùng có thể bấm tiếp nút khác. Đây là chi tiết nhỏ nhưng quyết định cảm giác "mượt" của editor.

### 2.4. Nút Upload ảnh

Trình duyệt không cho JS tự mở hộp thoại chọn file vì lý do bảo mật — phải do người dùng click. Nên dùng mẹo phổ biến: một `<input type="file">` ẩn, và nút icon chỉ "bấm hộ" nó:

```ts
openImagePicker() { this.imageInput.nativeElement.click(); }

onImageSelected(event: Event): void {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => {
    const url = reader.result as string;         // chuỗi base64 data URL
    this.body = `${this.body}\n![${file.name}](${url})\n`;
  };
  reader.readAsDataURL(file);
}
```

`FileReader` đọc file ảnh và mã hóa thành **data URL** (chuỗi base64 kiểu `data:image/png;base64,iVBOR...`). Chuỗi này nhúng thẳng ảnh vào markdown qua cú pháp `![alt](url)`, nên preview hiện ảnh ngay mà không cần server.

> **Lưu ý**: đây là giải pháp *tạm*. Base64 làm chuỗi `body` phình rất to (một ảnh vài trăm KB thành chuỗi text dài hàng trăm nghìn ký tự). Khi có backend, bạn sẽ upload file lên server và chỉ chèn URL thật trả về — nhẹ hơn nhiều.

### 2.5. Tags động

Trước đây 2 thẻ tag bị viết cứng trong HTML. Giờ chúng render từ mảng `tags` bằng vòng lặp `@for` của Angular:

```html
@for (tag of tags; track tag; let i = $index) {
  <span># {{ tag }}
    <span (click)="removeTag(i)">close</span>
  </span>
}
```

`track tag` giúp Angular biết thẻ nào là thẻ nào khi mảng đổi, để chỉ vẽ lại phần thay đổi thay vì vẽ lại tất cả (tối ưu hiệu năng). `addTag()` có kiểm tra trùng và rỗng trước khi thêm — validation cơ bản để dữ liệu sạch.

---

## Phần 3: Cách hiển thị nội dung — từ markdown thành HTML đẹp

Đây là nơi chuỗi `body` "sống dậy". Có ba lớp phối hợp.

### 3.1. Lớp 1 — Parser: `<markdown>` component

Preview dùng `MarkdownComponent` thay vì `MarkdownPipe`:

```html
<markdown [data]="body || defaultPreview"></markdown>
```

Bên trong, `ngx-markdown` dùng thư viện `marked` để phân tích markdown và sinh ra HTML tương ứng: `**x**` → `<strong>x</strong>`, ```` ```ts ```` → `<pre><code class="language-ts">`.

**Tại sao đổi từ Pipe sang Component?** Đây là quyết định kỹ thuật quan trọng nhất ở khâu hiển thị. `MarkdownPipe` chỉ chuyển text thành HTML rồi dừng. `MarkdownComponent` làm thêm một bước: sau khi render, nó gọi Prism để **tô màu code**. Nếu giữ Pipe, code block sẽ ra HTML trơn không màu. Muốn có syntax highlighting thì bắt buộc dùng component.

### 3.2. Lớp 2 — Syntax highlighting: Prism

Prism là thư viện tô màu code. Nó được nạp qua `angular.json` chứ không import trong component:

```json
"styles":  ["...", "node_modules/prismjs/themes/prism-tomorrow.css"],
"scripts": ["node_modules/prismjs/prism.js",
            "node_modules/prismjs/components/prism-typescript.min.js", "..."]
```

Cơ chế: Prism quét các thẻ `<code class="language-ts">` mà `marked` sinh ra, phân tích cú pháp ngôn ngữ đó, rồi bọc từng token (từ khóa, chuỗi, số...) vào `<span class="token keyword">`. File CSS theme `prism-tomorrow` định nghĩa màu cho từng loại token. Đó là lý do phải nạp **cả JS (logic tách token) lẫn CSS (màu)** — thiếu một trong hai là không có màu.

Chỉ nạp các ngôn ngữ cần (ts, js, bash, css, json) thay vì tất cả, để giữ bundle nhẹ. Muốn thêm ngôn ngữ khác chỉ cần thêm dòng `prism-<lang>.min.js`.

### 3.3. Lớp 3 — Typography: Tailwind `prose`

`marked` sinh HTML "trần" — thẻ `<h2>`, `<p>`, `<blockquote>` không có style. Để chúng đẹp, bọc trong class `prose`:

```html
<article class="prose prose-stone prose-lg max-w-none">
```

Class `prose` đến từ plugin `@tailwindcss/typography`. Nó tự động áp cỡ chữ, khoảng cách dòng, style cho heading/list/quote/link... một cách hài hòa. Đây là điểm dễ vấp: **trước khi cài plugin, class `prose` đã có trong HTML nhưng vô tác dụng** vì Tailwind không biết nó là gì. Phải kích hoạt bằng một dòng trong `styles.css`:

```css
@plugin "@tailwindcss/typography";
```

(Đây là cú pháp mới của Tailwind v4 — cấu hình plugin ngay trong CSS thay vì file `tailwind.config.js` như v3.)

### 3.4. Áp dụng chung cho trang detail

Cùng ba lớp đó, áp cho trang đọc bài `blog-detail-author`. Trước đây nội dung bài viết bị viết cứng bằng HTML thủ công. Giờ nó là một chuỗi markdown trong biến `content`, render qua `<markdown [data]="content">`. Kết quả: cùng một cơ chế, cùng style, cùng highlighting với editor — và về sau chỉ cần thay `content` bằng dữ liệu từ API là có trang đọc bài động.

---

## Tổng kết luồng hoàn chỉnh

```
Bấm icon Bold
   → wrapSelection chèn ** vào chuỗi body
   → [(ngModel)] cập nhật textarea (bạn thấy **text**)
   → <markdown [data]="body"> nhận chuỗi mới
      → marked: **text** → <strong>text</strong>
      → Prism: tô màu nếu là code block
      → prose: áp typography đẹp
   → Preview hiện chữ đậm
```

Ý tưởng nền tảng cần nhớ: **editor chỉ thao tác trên text thô; việc "đẹp" hoàn toàn nằm ở khâu render**. Tách bạch được hai việc này thì có thể mở rộng thoải mái — thêm nút mới chỉ là thêm một hàm chèn ký tự, không đụng gì tới phần hiển thị.

---

## Các file liên quan

| File | Vai trò |
|------|---------|
| `frontend/src/app/pages/blog/blog-creation-split/blog-creation-split.component.ts` | Logic toolbar, upload ảnh, tags |
| `frontend/src/app/pages/blog/blog-creation-split/blog-creation-split.component.html` | Giao diện editor + preview |
| `frontend/src/app/pages/blog/blog-detail-author/blog-detail-author.component.ts` | Nội dung markdown trang đọc bài |
| `frontend/src/app/app.config.ts` | Cấu hình `provideMarkdown` (gfm, breaks) |
| `frontend/angular.json` | Nạp Prism (theme CSS + scripts ngôn ngữ) |
| `frontend/src/styles.css` | Kích hoạt plugin `@tailwindcss/typography` |
