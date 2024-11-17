import {Component, OnDestroy} from "@angular/core";
import { Subscription } from "rxjs";
import {UserService} from "../../../common/service/user/user.service";
import {untilDestroyed} from "@ngneat/until-destroy";
import {Router} from "@angular/router";

@Component({
  selector: "texera-login-page",
  templateUrl: "./login-page.component.html",
  styleUrls: ["./login-page.component.scss"]
})
export class LoginPageComponent {
}
