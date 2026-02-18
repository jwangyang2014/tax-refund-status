import { describe, expect, it, vi } from "vitest";
import { login } from "../api/authApi";
import { fireEvent, render, screen } from "@testing-library/react";
import LoginPage from "../pages/LoginPage";

vi.mock('../api/authApi', () => ({
  login: vi.fn(),
}))

describe('LoginPage', () => {
  it('calls login and OnSuccess', async () => {
    const onSuccess = vi.fn();
    const onRegister = vi.fn();
    const onError = vi.fn();

    (login as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce(undefined);

    render(<LoginPage onSuccess={onSuccess} onRegister={onRegister} onError={onError}/>);

    fireEvent.change(screen.getByPlaceholderText('email'), { target: {value: 'yang@example.com'}});
    fireEvent.change(screen.getByPlaceholderText('password'), {target: {value: 'Password123'}});

    fireEvent.click(screen.getByRole('button', { name: 'Login'}));

    await screen.findByRole('button', { name: 'Login' });

    expect(login).toHaveBeenCalledWith('yang@example.com', 'Password123');
    expect(onSuccess).toHaveBeenCalledTimes(1);
    expect(onError).not.toHaveBeenCalled();
  });

  it('shows error via on Error when login fails', async () => {
    const onSuccess = vi.fn();
    const onRegister = vi.fn();
    const onError = vi.fn();

    (login as unknown as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error('Bad credentials'));

    render(<LoginPage onSuccess={onSuccess} onRegister={onRegister} onError={onError} />);

    fireEvent.click(screen.getByRole('button', { name: 'Login'}));

    await screen.findByRole('button', {name: 'Login'});

    expect(onSuccess).not.toHaveBeenCalled();
    expect(onError).toHaveBeenCalledWith('Bad credentials');
  });
})