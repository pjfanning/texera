import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { UserService } from "../../../../common/service/user/user.service";
import { mergeMap, tap  } from "rxjs/operators";
import { GoogleAuthService } from "../../../../common/service/user/google-auth.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DASHBOARD_USER_WORKFLOW } from "../../../../app-routing.constant";
import { ActivatedRoute, Router } from "@angular/router";
import { GoogleLoginProvider, SocialAuthService } from "@abacritt/angularx-social-login";

@UntilDestroy()
@Component({
  selector: "texera-google-login",
  templateUrl: "./google-login.component.html",
})
export class GoogleLoginComponent implements OnInit {
  isLoggedin?: boolean = false;

  @ViewChild("googleButton") googleButton!: ElementRef;
  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private googleAuthService: GoogleAuthService,
    private router: Router,
    private elementRef: ElementRef,
    private socialAuthService: SocialAuthService
  ) {}

  ngOnInit() {
    // this.isLoggedin = this.userService.isLogin();
    this.socialAuthService.authState
      .pipe(
        tap(res => {
          console.log(res.idToken);
          console.log("ID Token Type:", typeof res.idToken);
        }),
        mergeMap(res => this.userService.googleLogin(res.idToken)),
        untilDestroyed(this)
      )
      .subscribe(() => {
        this.router.navigateByUrl(this.route.snapshot.queryParams["returnUrl"] || DASHBOARD_USER_WORKFLOW);
      });
  }

  loginWithGoogle(): void {
    this.socialAuthService.signIn(GoogleLoginProvider.PROVIDER_ID);
  }

  logOut(): void {
    this.socialAuthService.signOut();
  }

  // ngAfterViewInit(): void {
  //   this.googleAuthService.googleAuthInit(this.elementRef.nativeElement);
  //   this.googleAuthService.googleCredentialResponse
  //     .pipe(
  //       mergeMap(res => this.userService.googleLogin(res.credential)),
  //       untilDestroyed(this)
  //     )
  //     .subscribe(() => {
  //       this.router.navigateByUrl(this.route.snapshot.queryParams["returnUrl"] || DASHBOARD_USER_WORKFLOW);
  //     });
  // }
}
