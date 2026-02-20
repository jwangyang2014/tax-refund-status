import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

vi.mock('../api/authApi', () => ({
  register: vi.fn()
}));

import { register } from '../api/authApi';
import RegisterPage from '../pages/RegisterPage';

describe('RegisterPage', () => {
  it('calls register and onSuccess', async () => {
    const onSuccess = vi.fn();
    const onBack = vi.fn();
    const onError = vi.fn();

    (register as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined);

    render(<RegisterPage onSuccess={onSuccess} onBack={onBack} onError={onError} />);

    fireEvent.change(screen.getByPlaceholderText('email'), { target: { value: 'a@b.com' } });
    fireEvent.change(screen.getByPlaceholderText('password'), { target: { value: 'Password123!' } });
    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    await screen.findByRole('button', { name: 'Register' });

    expect(register).toHaveBeenCalledWith('a@b.com', 'Password123!');
    expect(onSuccess).toHaveBeenCalledTimes(1);
    expect(onError).not.toHaveBeenCalled();
  });

  it('reports error via onError when register fails', async () => {
    const onSuccess = vi.fn();
    const onBack = vi.fn();
    const onError = vi.fn();

    (register as unknown as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('Email already registered'));

    render(<RegisterPage onSuccess={onSuccess} onBack={onBack} onError={onError} />);

    fireEvent.click(screen.getByRole('button', { name: 'Register' }));
    await screen.findByRole('button', { name: 'Register' });

    expect(onSuccess).not.toHaveBeenCalled();
    expect(onError).toHaveBeenCalledWith('Email already registered');
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
