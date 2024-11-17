import {Component, OnDestroy} from "@angular/core";
import { Subscription } from "rxjs";

@Component({
  selector: "texera-login-page",
  templateUrl: "./login-page.component.html",
  styleUrls: ["./login-page.component.scss"]
})
export class LoginPageComponent implements OnDestroy {
  private subscriptions = new Subscription();

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }
}
