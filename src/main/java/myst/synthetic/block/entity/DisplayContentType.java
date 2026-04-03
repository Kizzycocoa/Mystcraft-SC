package myst.synthetic.block.entity;

public enum DisplayContentType {
	EMPTY,
	PAPER,
	PAGE,
	MAP,
	WRITABLE_BOOK,
	WRITTEN_BOOK,
	LINKING_BOOK,
	DESCRIPTIVE_BOOK;

	public boolean isBookLike() {
		return this == WRITABLE_BOOK
				|| this == WRITTEN_BOOK
				|| this == LINKING_BOOK
				|| this == DESCRIPTIVE_BOOK;
	}

	public boolean isPageLike() {
		return this == PAPER || this == PAGE;
	}
}