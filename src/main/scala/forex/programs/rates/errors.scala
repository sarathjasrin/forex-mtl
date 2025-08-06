package forex.programs.rates

import forex.services.rates.errors.{Error => RatesServiceError}

object errors {

  sealed trait Error extends Exception

  object Error {
    final case class RateLookupFailed(msg: String) extends Error

    final case class UnsupportedCurrency(code: String) extends Error

    final case class InvalidPairFormat(pair: String) extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
  }
}
