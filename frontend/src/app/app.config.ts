import { ApplicationConfig, APP_INITIALIZER, provideZoneChangeDetection, PLATFORM_ID, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideMarkdown, MARKED_OPTIONS, CLIPBOARD_OPTIONS } from 'ngx-markdown';
import { apiInterceptor } from './core/interceptors/api.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { CustomClipboardButtonComponent } from './components/custom-clipboard-button/custom-clipboard-button.component';
import { AuthService } from './core/services/auth.service';
import { isPlatformBrowser } from '@angular/common';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';

export function initializeApp() {
  const authService = inject(AuthService);
  const platformId = inject(PLATFORM_ID);

  return () => {
    if (isPlatformBrowser(platformId)) {
      return authService.getProfile().pipe(
        tap((res) => authService.currentUser.set(res.data)),
        catchError(() => {
          authService.currentUser.set(null);
          return of(null);
        })
      );
    }
    return of(null);
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptors([apiInterceptor, errorInterceptor])),
    provideAnimations(),
    provideToastr({
      timeOut: 3000,
      positionClass: 'toast-top-right',
      preventDuplicates: true,
    }),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [AuthService],
      multi: true,
    },
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

