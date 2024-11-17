import { Injectable } from "@angular/core";
import { Subject } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { AppSettings } from "../../app-setting";
declare var window: any;
export interface CredentialResponse {
  client_id: string;
  credential: string;
  select_by: string;
}
@Injectable({
  providedIn: "root",
})
export class GoogleAuthService {
  private _googleCredentialResponse = new Subject<CredentialResponse>();
  constructor(private http: HttpClient) {}
  // public googleAuthInit(parent: HTMLElement | null) {
  //   if(parent){
  //     console.log("have parent")
  //   } else {
  //     console.log("no parent")
  //   }
  //   this.http.get(`${AppSettings.getApiEndpoint()}/auth/google/clientid`, { responseType: "text" }).subscribe({
  //     next: response => {
  //       window.onGoogleLibraryLoad = () => {
  //         window.google.accounts.id.initialize({
  //           client_id: response,
  //           callback: (auth: CredentialResponse) => {
  //             this._googleCredentialResponse.next(auth);
  //           },
  //         });
  //         window.google.accounts.id.renderButton(parent, { width: 270 });
  //         window.google.accounts.id.prompt();
  //       };
  //     },
  //     error: (err: unknown) => {
  //       console.error(err);
  //     },
  //   });
  // }

  public googleAuthInit(parent: HTMLElement | null) {
    if (!parent) {
      console.error("Parent element is null. Cannot render Google login button.");
      return;
    }
    console.log("Parent element exists in DOM:", document.body.contains(parent));

    this.http.get(`${AppSettings.getApiEndpoint()}/auth/google/clientid`, { responseType: "text" }).subscribe({
      next: response => {
        console.log("Google Client ID received:", response);

        window.onGoogleLibraryLoad = () => {
          if (window.google && window.google.accounts && window.google.accounts.id) {
            console.log("Google API loaded successfully.");

            window.google.accounts.id.initialize({
              client_id: response,
              callback: (auth: CredentialResponse) => {
                console.log("Google Credential Response received:", auth);
                this._googleCredentialResponse.next(auth);
              },
            });

            if (document.body.contains(parent)) {
              window.google.accounts.id.renderButton(parent, { width: 270 });
              console.log("Google login button rendered successfully.");
            } else {
              console.error("Parent element is not attached to DOM.");
            }

            window.google.accounts.id.prompt();
          } else {
            console.error("Google API not loaded. Ensure the library is properly included.");
          }
        };
      },
      error: (err: unknown) => {
        console.error("Error fetching Google Client ID:", err);
      },
    });
  }

  get googleCredentialResponse() {
    return this._googleCredentialResponse.asObservable();
  }

  public cleanGoogle(){
    if (window.google?.accounts?.id) {
      window.google.accounts.id.cancel(); // 取消任何挂起的操作
    }
  }
}
