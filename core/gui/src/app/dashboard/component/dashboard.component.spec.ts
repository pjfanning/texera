import { TestBed, ComponentFixture } from "@angular/core/testing";
import { DashboardComponent } from "./dashboard.component";
import { NO_ERRORS_SCHEMA, ChangeDetectorRef, NgZone, EventEmitter } from "@angular/core";
import { By } from "@angular/platform-browser";
import { of } from "rxjs";

import { UserService } from "../../common/service/user/user.service";
import { FlarumService } from "../service/user/flarum/flarum.service";
import { SocialAuthService } from "@abacritt/angularx-social-login";
import {
  Router,
  NavigationEnd,
  ActivatedRoute,
  ActivatedRouteSnapshot,
  UrlSegment,
  Params,
  Data,
} from "@angular/router";
import { convertToParamMap } from "@angular/router";

describe("DashboardComponent", () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  let userServiceMock: Partial<UserService>;
  let routerMock: Partial<Router>;
  let flarumServiceMock: Partial<FlarumService>;
  let cdrMock: Partial<ChangeDetectorRef>;
  let ngZoneMock: Partial<NgZone>;
  let socialAuthServiceMock: Partial<SocialAuthService>;
  let activatedRouteMock: Partial<ActivatedRoute>;

  const activatedRouteSnapshotMock: Partial<ActivatedRouteSnapshot> = {
    queryParams: {},
    url: [] as UrlSegment[],
    params: {} as Params,
    fragment: null,
    data: {} as Data,
    paramMap: convertToParamMap({}),
    queryParamMap: convertToParamMap({}),
    outlet: "",
    routeConfig: null,
    root: null as any,
    parent: null as any,
    firstChild: null as any,
    children: [],
    pathFromRoot: [],
  };

  beforeEach(async () => {
    userServiceMock = {
      isAdmin: jasmine.createSpy("isAdmin").and.returnValue(false),
      isLogin: jasmine.createSpy("isLogin").and.returnValue(false),
      userChanged: jasmine.createSpy("userChanged").and.returnValue(of(null)),
    };

    routerMock = {
      events: of(new NavigationEnd(1, "/dashboard", "/dashboard")),
      url: "/dashboard",
      navigateByUrl: jasmine.createSpy("navigateByUrl"),
    };

    flarumServiceMock = {
      auth: jasmine.createSpy("auth").and.returnValue(of({ token: "dummyToken" })),
      register: jasmine.createSpy("register").and.returnValue(of(null)),
    };

    cdrMock = {
      detectChanges: jasmine.createSpy("detectChanges"),
    };

    ngZoneMock = {
      hasPendingMicrotasks: false,
      hasPendingMacrotasks: false,
      onUnstable: new EventEmitter<any>(),
      onMicrotaskEmpty: new EventEmitter<any>(),
      onStable: new EventEmitter<any>(),
      onError: new EventEmitter<any>(),
      run: (fn: () => any) => fn(),
      runGuarded: (fn: () => any) => fn(),
      runOutsideAngular: (fn: () => any) => fn(),
      runTask: (fn: () => any) => fn(),
    };

    socialAuthServiceMock = {
      authState: of(),
    };

    activatedRouteMock = {
      snapshot: activatedRouteSnapshotMock as ActivatedRouteSnapshot,
    };

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: FlarumService, useValue: flarumServiceMock },
        { provide: ChangeDetectorRef, useValue: cdrMock },
        { provide: NgZone, useValue: ngZoneMock },
        { provide: SocialAuthService, useValue: socialAuthServiceMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create the component", () => {
    expect(component).toBeTruthy();
  });

  it("should render Google sign-in button when user is NOT logged in", () => {
    (userServiceMock.isLogin as jasmine.Spy).and.returnValue(false);
    fixture.detectChanges();

    const googleSignInBtn = fixture.debugElement.query(By.css("asl-google-signin-button"));
    expect(googleSignInBtn).toBeTruthy();
  });
});
