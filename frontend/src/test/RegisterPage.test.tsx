import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach } from 'vitest';

vi.mock('../api/authApi', () => ({
  register: vi.fn()
}));

import { register } from '../api/authApi';
import RegisterPage from '../pages/RegisterPage';

const mockRegister = register as unknown as ReturnType<typeof vi.fn>;

function fillRequiredFields() {
  fireEvent.change(screen.getByLabelText(/^Email/i), { target: { value: 'a@b.com' } });
  fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'Password123!' } });
  fireEvent.change(screen.getByLabelText(/^Repeat Password/i), { target: { value: 'Password123!' } });
  fireEvent.change(screen.getByLabelText(/^First Name/i), { target: { value: 'Yang' } });
  fireEvent.change(screen.getByLabelText(/^Last Name/i), { target: { value: 'Wang' } });
  fireEvent.change(screen.getByLabelText(/^City/i), { target: { value: 'Mountain View' } });
  fireEvent.change(screen.getByLabelText(/^State/i), { target: { value: 'CA' } });
  // phone + address are optional
}

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('calls register and onSuccess', async () => {
    const onSuccess = vi.fn();
    const onBack = vi.fn();
    const onError = vi.fn();

    mockRegister.mockResolvedValueOnce(undefined);

    render(<RegisterPage onSuccess={onSuccess} onBack={onBack} onError={onError} />);

    fillRequiredFields();

    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledTimes(1);
    });

    // Assert the *object payload* (new API shape)
    expect(mockRegister).toHaveBeenCalledWith({
      email: 'a@b.com',
      password: 'Password123!',
      firstName: 'Yang',
      lastName: 'Wang',
      address: null,
      city: 'Mountain View',
      state: 'CA',
      phone: null
    });

    expect(onSuccess).toHaveBeenCalledTimes(1);
    expect(onError).not.toHaveBeenCalled();
  });

  it('reports error via onError when register fails', async () => {
    const onSuccess = vi.fn();
    const onBack = vi.fn();
    const onError = vi.fn();

    mockRegister.mockRejectedValueOnce(new Error('Email already registered'));

    render(<RegisterPage onSuccess={onSuccess} onBack={onBack} onError={onError} />);

    fillRequiredFields();
    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() => expect(onError).toHaveBeenCalledWith('Email already registered'));
    expect(onSuccess).not.toHaveBeenCalled();
  });

  it('back button calls onBack', () => {
    const onSuccess = vi.fn();
    const onBack = vi.fn();
    const onError = vi.fn();

    render(<RegisterPage onSuccess={onSuccess} onBack={onBack} onError={onError} />);

    fireEvent.click(screen.getByRole('button', { name: 'Back' }));
    expect(onBack).toHaveBeenCalledTimes(1);
  });
});