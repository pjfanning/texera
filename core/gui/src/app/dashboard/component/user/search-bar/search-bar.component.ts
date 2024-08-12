import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { SearchService } from "../../../service/user/search.service";
import { SearchFilterParameters } from "../../../type/search-filter-parameters";
import { SortMethod } from "../../../type/sort-method";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { SearchResult, SearchResultItem } from "../../../type/search-result";
import { DashboardEntry } from "../../../type/dashboard-entry";
import { Subject } from "rxjs";
import { debounceTime, switchMap } from "rxjs/operators";

@UntilDestroy()
@Component({
  selector: "texera-search-bar",
  templateUrl: "./search-bar.component.html",
  styleUrls: ["./search-bar.component.scss"],
})
export class SearchBarComponent {
  public searchParam: string = "";
  public listOfResult: string[] = [];
  private searchSubject = new Subject<string>();

  private params: SearchFilterParameters = {
    createDateStart: null,
    createDateEnd: null,
    modifiedDateStart: null,
    modifiedDateEnd: null,
    owners: [],
    ids: [],
    operators: [],
    projectIds: [],
  };

  constructor(
    private router: Router,
    private searchService: SearchService,
  ) {
    this.searchSubject.pipe(
      debounceTime(200),
      switchMap(query => this.searchService.search([query], this.params, 0, 5, null, SortMethod.NameAsc)),
      untilDestroyed(this)
    ).subscribe((result: SearchResult) => {
      this.listOfResult = result.results.map(item => this.convertToName(item));
      console.log(this.listOfResult)
    });
  }

  onSearchInputChange(query: string): void {
    this.searchSubject.next(query);
  }

  performSearch(keyword: string) {
    this.router.navigate(["/dashboard/search"], { queryParams: { q: keyword } });
  }

  convertToName(resultItem: SearchResultItem): string{
    if (resultItem.workflow) {
      return new DashboardEntry(resultItem.workflow).name;
    } else if (resultItem.project) {
      return new DashboardEntry(resultItem.project).name;
    } else if (resultItem.file) {
      return new DashboardEntry(resultItem.file).name;
    } else if (resultItem.dataset) {
      return new DashboardEntry(resultItem.dataset).name;
    } else {
      throw new Error("Unexpected type in SearchResult.");
    }
  }
}
