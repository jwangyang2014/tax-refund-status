export type RefundStatusResponse = {
  taxYear: number;
  status: string;
  lastUpdatedAt: string;
  expectedAMount: number | null;
  trackingId: string | null;
  availableAtEstimated: string | null;
  aiExplanation: string | null;
}