import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideMarkdown, MARKED_OPTIONS, CLIPBOARD_OPTIONS } from 'ngx-markdown';
import { apiInterceptor } from './core/interceptors/api.interceptor';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { CustomClipboardButtonComponent } from './components/custom-clipboard-button/custom-clipboard-button.component';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptors([apiInterceptor, jwtInterceptor])),
    provideMarkdown({
      markedOptions: {
        provide: MARKED_OPTIONS,
        useValue: {
          gfm: true,
          breaks: true,
        },
      },
      clipboardOptions: {
        provide: CLIPBOARD_OPTIONS,
        useValue: {
          buttonComponent: CustomClipboardButtonComponent,
        },
      },
    }),
  ],
};

