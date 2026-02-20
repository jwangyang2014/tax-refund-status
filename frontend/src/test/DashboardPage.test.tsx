import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

vi.mock('../api/refundApi', () => ({
  getLatestRefund: vi.fn(),
  simulateRefundUpdate: vi.fn()
}));

import { getLatestRefund, simulateRefundUpdate } from '../api/refundApi';
import DashboardPage from '../pages/DashboardPage';

describe('DashboardPage', () => {
  it('loads and displays refund status', async () => {
    const onLogout = vi.fn();
    const onError = vi.fn();

    (getLatestRefund as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      taxYear: 2025,
      status: 'PROCESSING',
      lastUpdatedAt: new Date().toISOString(),
      expectedAmount: 123.45,
      trackingId: 'IRS-1',
      availableAtEstimated: new Date(Date.now() + 86400000).toISOString(),
      aiExplanation: 'ETA based on stage'
    });

    render(<DashboardPage onLogout={onLogout} onError={onError} />);

    expect(await screen.findByText(/Status:/)).toBeInTheDocument();
    expect(screen.getByText(/PROCESSING/)).toBeInTheDocument();
    expect(onError).not.toHaveBeenCalled();
  });

  it('refresh button calls load again', async () => {
    const onLogout = vi.fn();
    const onError = vi.fn();

    (getLatestRefund as unknown as ReturnType<typeof vi.fn>)
      .mockResolvedValueOnce({
        taxYear: 2025,
        status: 'RECEIVED',
        lastUpdatedAt: new Date().toISOString(),
        expectedAmount: 1,
        trackingId: 'T1',
        availableAtEstimated: null,
        aiExplanation: null
      })
      .mockResolvedValueOnce({
        taxYear: 2025,
        status: 'PROCESSING',
        lastUpdatedAt: new Date().toISOString(),
        expectedAmount: 1,
        trackingId: 'T1',
        availableAtEstimated: null,
        aiExplanation: null
      });

    render(<DashboardPage onLogout={onLogout} onError={onError} />);

    await screen.findByText(/RECEIVED/);
    fireEvent.click(screen.getByRole('button', { name: 'Refresh' }));

    await screen.findByText(/PROCESSING/);
    expect(getLatestRefund).toHaveBeenCalledTimes(2);
  });

  it('demo advance calls simulate then reload', async () => {
    const onLogout = vi.fn();
    const onError = vi.fn();

    (getLatestRefund as unknown as ReturnType<typeof vi.fn>)
      .mockResolvedValueOnce({
        taxYear: 2025,
        status: 'RECEIVED',
        lastUpdatedAt: new Date().toISOString(),
        expectedAmount: 10,
        trackingId: 'T1',
        availableAtEstimated: null,
        aiExplanation: null
      })
      .mockResolvedValueOnce({
        taxYear: 2025,
        status: 'PROCESSING',
        lastUpdatedAt: new Date().toISOString(),
        expectedAmount: 10,
        trackingId: 'T1',
        availableAtEstimated: null,
        aiExplanation: null
      });

    (simulateRefundUpdate as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined);

    render(<DashboardPage onLogout={onLogout} onError={onError} />);

    await screen.findByText(/RECEIVED/);
    fireEvent.click(screen.getByRole('button', { name: 'Demo: Advance Status' }));

    await screen.findByText(/PROCESSING/);

    expect(simulateRefundUpdate).toHaveBeenCalledWith({
      taxYear: 2025,
      status: 'PROCESSING',
      expectedAmount: 10,
      trackingId: 'T1'
    });
  });

  it('logout calls onLogout', async () => {
    const onLogout = vi.fn();
    const onError = vi.fn();

    (getLatestRefund as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      taxYear: 2025,
      status: 'RECEIVED',
      lastUpdatedAt: new Date().toISOString(),
      expectedAmount: 1,
      trackingId: 'T1',
      availableAtEstimated: null,
      aiExplanation: null
    });

    render(<DashboardPage onLogout={onLogout} onError={onError} />);
    await screen.findByText(/RECEIVED/);

    fireEvent.click(screen.getByRole('button', { name: 'Logout' }));
    expect(onLogout).toHaveBeenCalledTimes(1);
  });
});
