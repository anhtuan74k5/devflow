/**
 * Formats a UTC date string to the user's local timezone for display.
 *
 * The backend stores and returns all dates in UTC format (ISO-8601 with 'Z' suffix).
 * This function converts the UTC timestamp to the user's local timezone
 * and returns a human-readable string.
 *
 * @param utcDateString - ISO-8601 UTC date string (e.g., "2026-07-08T08:00:00Z")
 * @returns Formatted date string in the user's locale
 */
export function formatUTCDate(utcDateString: string): string {
  const date = new Date(utcDateString);
  return date.toLocaleString();
}
