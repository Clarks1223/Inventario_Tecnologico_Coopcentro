import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import PageHeader from '../PageHeader'

describe('PageHeader', () => {
  it('renders title', () => {
    render(<PageHeader title="Test Title" />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('renders children', () => {
    render(
      <PageHeader title="Test">
        <button>Action</button>
      </PageHeader>
    )
    expect(screen.getByText('Action')).toBeInTheDocument()
  })
})
