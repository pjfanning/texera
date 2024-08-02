import { Component } from "@angular/core";


@Component({
  selector: 'texera-list-item',
  templateUrl: './list-item.component.html',
  styleUrls: ['./list-item.component.scss'],
})
export class ListItemComponent {
  public entryLink: string = "";
}
