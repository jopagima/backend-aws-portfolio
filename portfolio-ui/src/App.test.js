import { render, screen } from '@testing-library/react';
import App from './App';

test('debe mostrar el botón de inicio de sesión', () => {
  render(<App />);
  const loginButton = screen.getByText(/Sign in/i);
  expect(loginButton).toBeInTheDocument();
});
