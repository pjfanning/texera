import { Subject } from "rxjs";
import { Injectable } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class PanelService {
  private openPanelSubject = new Subject<void>();
  private closePanelSubject = new Subject<void>();
  private resetPanelSubject = new Subject<void>();

  get resetPanelStream() {
    return this.resetPanelSubject.asObservable();
  }

  resetPanels() {
    this.resetPanelSubject.next();
  }

  get closePanelStream() {
    return this.closePanelSubject.asObservable();
  }

  closePanels() {
    this.closePanelSubject.next();
  }

  get openPanelStream() {
    return this.openPanelSubject.asObservable();
  }

  openPanels() {
    this.openPanelSubject.next();
  }
}
