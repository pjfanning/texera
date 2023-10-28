import {UntilDestroy} from "@ngneat/until-destroy";
import {Component, OnInit} from "@angular/core";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-workflow-environment-select.component',
  templateUrl: './ngbd-modal-workflow-environment-select.component.html',
  styleUrls: ['./ngbd-modal-workflow-environment-select.component.scss']
})
export class NgbdModalWorkflowEnvironmentSelectComponent implements OnInit {
  ngOnInit(): void {
  }

}
