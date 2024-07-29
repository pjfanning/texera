import { Component, Input, Output, EventEmitter } from "@angular/core";

@Component({
  selector: "texera-pagination-component",
  templateUrl: "./pagination.component.html",
  styleUrls: ["./pagination.component.scss"]
})
export class PaginationComponent {
  @Input() currentPage: number = 1;
  @Input() totalPages: number = 1;
  @Output() pageChange = new EventEmitter<number>();

  maxPagesToShow: number = 5;
  userInputPage: number = this.currentPage;

  getPages(): number[] {
    const pages: number[] = [];
    const halfPagesToShow = Math.floor(this.maxPagesToShow / 2);

    let startPage = Math.max(this.currentPage - halfPagesToShow, 1);
    let endPage = Math.min(startPage + this.maxPagesToShow - 1, this.totalPages);

    if (endPage - startPage < this.maxPagesToShow - 1) {
      startPage = Math.max(endPage - this.maxPagesToShow + 1, 1);
    }

    for (let page = startPage; page <= endPage; page++) {
      pages.push(page);
    }

    return pages;
  }

  setPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.pageChange.emit(page);
    }
  }

  onUserInputChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.userInputPage = Number(value);
  }

  goToPage(): void {
    if (this.userInputPage >= 1 && this.userInputPage <= this.totalPages) {
      this.setPage(this.userInputPage);
    } else {
      alert(`Please enter a valid page number between 1 and ${this.totalPages}.`);
    }
  }
}
