import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const isExternalUrl = req.url.startsWith('http://') || req.url.startsWith('https://');

  let apiReq = req;
  if (!isExternalUrl) {
    const urlPath = req.url.startsWith('/') ? req.url.substring(1) : req.url;
    apiReq = req.clone({
      url: `${environment.apiUrl}/${urlPath}`,
      withCredentials: true
    });
  }

  return next(apiReq);
};
