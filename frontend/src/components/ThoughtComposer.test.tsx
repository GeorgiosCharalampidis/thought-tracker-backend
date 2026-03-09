import '@testing-library/jest-dom';
import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import ThoughtComposer from './ThoughtComposer';

describe('ThoughtComposer', () => {
  it('renders the heading and calls onSubmit from the send button', () => {
    const handleSubmit = jest.fn();

    render(
      <ThoughtComposer
        headingText="Share a thought"
        thoughtPlaceholder="Write a thought..."
        note="Something on my mind"
        validationMessage=""
        loading={false}
        isDarkMode={false}
        onChange={() => {}}
        onSubmit={handleSubmit}
      />
    );

    expect(screen.getByText('Share a thought')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button'));
    expect(handleSubmit).toHaveBeenCalledTimes(1);
  });
});
