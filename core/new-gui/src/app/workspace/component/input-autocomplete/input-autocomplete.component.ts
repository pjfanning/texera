import { Component } from "@angular/core";
import { FieldType, FieldTypeConfig } from "@ngx-formly/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { UserFileService } from "src/app/dashboard/user/service/user-file/user-file.service";
import { debounceTime } from "rxjs/operators";
import { map } from "rxjs";

@UntilDestroy()
@Component({
  selector: "texera-input-autocomplete-template",
  templateUrl: "./input-autocomplete.component.html",
  styleUrls: ["input-autocomplete.component.scss"],
})
export class InputAutoCompleteComponent extends FieldType<FieldTypeConfig> {
  // the autocomplete selection list
  public suggestions: string[] = [];

  constructor(public userFileService: UserFileService) {
    super();
  }

  autocomplete(): void {
    if (this.field.formControl.value === null) {
      return;
    }
    // currently it's a hard-code UserFileService autocomplete
    // TODO: generalize this callback function with a formly hook.
    const value = this.field.formControl.value.trim();
    if (value.length > 0) {
      // perform auto-complete based on the current input
      this.userFileService
        .getAutoCompleteFileList(value)
        .pipe(debounceTime(300))
        .pipe(untilDestroyed(this))
        .subscribe(suggestedFiles => {
          const updated =
            this.suggestions.length != suggestedFiles.length ||
            this.suggestions.some((e, i) => e !== suggestedFiles[i]);
          if (updated) this.suggestions = [...suggestedFiles];
        });
    } else {
      // no valid input, perform full scan
      this.userFileService
        .getFileList()
        .pipe(map(list => list.map(x => x.ownerEmail + "/" + x.file.name)))
        .pipe(untilDestroyed(this))
        .subscribe(allAccessibleFiles => (this.suggestions = allAccessibleFiles));
    }
  }
}
